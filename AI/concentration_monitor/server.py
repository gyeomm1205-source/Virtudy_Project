from fastapi import FastAPI, HTTPException, WebSocket, WebSocketDisconnect
from pydantic import BaseModel
import asyncio
import multiprocessing
import uvicorn
import logging
from run_livekit import run_bot
import json

# Logger setup
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("AI_Server")

app = FastAPI()

# Global dictionary to store queues: {room_id: Queue}
# multiprocessing.Manager will be initialized in main block if needed, 
# but for simplicity with uvicorn workers, we need a way to share.
# However, since we spawn Process from here, we can pass the queue directly.
# We need to store it to access it in websocket_endpoint.
active_queues = {} 

class JoinRequest(BaseModel):
    url: str
    token: str
    room_id: str

def bot_process(url: str, token: str, queue: multiprocessing.Queue):
    """
    Wrapper to run the async bot in a separate process.
    """
    print(f"[DEBUG] Bot Process Spawned! (PID: {multiprocessing.current_process().pid})", flush=True)
    try:
        asyncio.run(run_bot(url, token, queue))
    except Exception as e:
        print(f"[ERROR] Bot Process Crashed: {e}", flush=True)
        logger.error(f"Bot execution failed: {e}")

@app.post("/bot/join")
async def join_room(request: JoinRequest):
    """
    Triggers a new AI bot instance to join a LiveKit room.
    """
    logger.info(f"Received request to join room: {request.room_id}")
    
    # Create a shared queue for this room using standard multiprocessing.Queue
    # (Since we are forking/spawning from this process, it works)
    queue = multiprocessing.Queue()
    active_queues[request.room_id] = queue

    # Spawn a new process for the bot so it doesn't block the API server
    p = multiprocessing.Process(target=bot_process, args=(request.url, request.token, queue))
    p.start()
    
    return {"status": "started", "pid": p.pid, "message": f"Bot joining room {request.room_id}"}

@app.websocket("/ws/analysis/{room_id}/{member_id}")
async def websocket_endpoint(websocket: WebSocket, room_id: str, member_id: str):
    """
    Handles WebSocket connections from the frontend.
    """
    await websocket.accept()
    logger.info(f"WebSocket connected: Room {room_id}, Member {member_id}")
    
    queue = active_queues.get(room_id)
    if not queue:
        logger.warning(f"No active AI bot found for room {room_id}. Queue Missing!")
    else:
        logger.info(f"Queue found for room {room_id}. Listening for data...")
    
    try:
        while True:
            if queue and not queue.empty():
                data = queue.get()
                # logger.info(f"Sending data to WS: {data}") # Too noisy, uncomment if needed
                print(f"[DEBUG] Sending to FE: {data['value']} ({data['eventType']})") # Explicit print for user
                await websocket.send_json(data)
            else:
                await asyncio.sleep(0.01) # Faster polling
    except WebSocketDisconnect:
        logger.info(f"WebSocket disconnected: Room {room_id}, Member {member_id}")
    except Exception as e:
        logger.error(f"WebSocket error: {e}")

@app.get("/health")
async def health_check():
    return {"status": "ok"}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
