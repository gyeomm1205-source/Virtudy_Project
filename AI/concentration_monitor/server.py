import os
import sys

# Ensure the current directory is in the python path
current_dir = os.path.dirname(os.path.abspath(__file__))
if current_dir not in sys.path:
    sys.path.insert(0, current_dir)

from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from pydantic import BaseModel
import asyncio
import multiprocessing
import uvicorn
import logging
from run_livekit import run_bot

# Logger setup
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("AI_Server")

app = FastAPI()

# [Fix] Add CORS Middleware to allow connections from Frontend
from fastapi.middleware.cors import CORSMiddleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allow all origins for testing
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global dictionary to store queues: {room_id: Queue}
# multiprocessing.Manager will be initialized in main block if needed, 
# but for simplicity with uvicorn workers, we need a way to share.
# However, since we spawn Process from here, we can pass the queue directly.
# We need to store it to access it in websocket_endpoint.
active_queues = {}
active_connections = {}  # {room_id: {member_id: WebSocket}}
dispatch_tasks = {}      # {room_id: asyncio.Task}

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
@app.post("/fastapi/bot/join")
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

async def dispatch_loop(room_id: str):
    try:
        while True:
            queue = active_queues.get(room_id)
            conns = active_connections.get(room_id, {})
            if not conns:
                await asyncio.sleep(0.05)
                continue
            if queue and not queue.empty():
                data = queue.get()
                participant_id = data.get("participantId")
                if participant_id:
                    ws = conns.get(participant_id)
                    if ws:
                        await ws.send_json(data)
                else:
                    for ws in list(conns.values()):
                        await ws.send_json(data)
            else:
                await asyncio.sleep(0.01)
    except asyncio.CancelledError:
        return

@app.websocket("/ws/analysis/{room_id}/{member_id}")
@app.websocket("/fastapi/ws/analysis/{room_id}/{member_id}")
async def websocket_endpoint(websocket: WebSocket, room_id: str, member_id: str):
    """
    Handles WebSocket connections from the frontend.
    """
    await websocket.accept()
    logger.info(f"WebSocket connected: Room {room_id}, Member {member_id}")
    
    try:
        # Register connection
        room_conns = active_connections.setdefault(room_id, {})
        room_conns[member_id] = websocket

        # Start dispatcher for this room if not running
        if room_id not in dispatch_tasks:
            dispatch_tasks[room_id] = asyncio.create_task(dispatch_loop(room_id))

        # Keep the connection open; messages are pushed by dispatcher
        while True:
            await asyncio.sleep(1.0)
    except WebSocketDisconnect:
        logger.info(f"WebSocket disconnected: Room {room_id}, Member {member_id}")
    except Exception as e:
        logger.error(f"WebSocket error: {e}")
    finally:
        # Cleanup connection
        room_conns = active_connections.get(room_id, {})
        room_conns.pop(member_id, None)
        if not room_conns:
            active_connections.pop(room_id, None)
            task = dispatch_tasks.pop(room_id, None)
            if task:
                task.cancel()

@app.get("/health")
async def health_check():
    return {"status": "ok"}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
