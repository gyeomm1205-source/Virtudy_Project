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
import time
from run_livekit import run_bot
from queue import Empty

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
active_bots = {}  # {room_id: multiprocessing.Process}
active_connections = {}  # {room_id: {member_id: WebSocket}}
dispatch_tasks = {}      # {room_id: asyncio.Task}
last_ai_payloads = {}  # {room_id: {member_id: {"status": payload, "score": payload}}}
room_empty_since = {}  # {room_id: timestamp}
WS_EMPTY_GRACE_SEC = 5.0

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

def _terminate_room_bot(room_id: str):
    task = dispatch_tasks.pop(room_id, None)
    if task and task is not asyncio.current_task():
        task.cancel()
    bot = active_bots.pop(room_id, None)
    if bot and bot.is_alive():
        bot.terminate()
        bot.join(timeout=2)
    active_queues.pop(room_id, None)
    active_connections.pop(room_id, None)
    last_ai_payloads.pop(room_id, None)
    room_empty_since.pop(room_id, None)

@app.post("/bot/join")
@app.post("/fastapi/bot/join")
async def join_room(request: JoinRequest):
    """
    Triggers a new AI bot instance to join a LiveKit room.
    """
    logger.info(f"Received request to join room: {request.room_id}")
    
    existing_bot = active_bots.get(request.room_id)
    if existing_bot and existing_bot.is_alive():
        return {"status": "already_running", "pid": existing_bot.pid, "message": f"Bot already running for room {request.room_id}"}

    if existing_bot:
        active_bots.pop(request.room_id, None)
        active_queues.pop(request.room_id, None)
        last_ai_payloads.pop(request.room_id, None)
        room_empty_since.pop(request.room_id, None)

    # Create a shared queue for this room using standard multiprocessing.Queue
    # (Since we are forking/spawning from this process, it works)
    queue = multiprocessing.Queue()
    active_queues[request.room_id] = queue

    # Spawn a new process for the bot so it doesn't block the API server
    p = multiprocessing.Process(target=bot_process, args=(request.url, request.token, queue))
    p.start()
    active_bots[request.room_id] = p
    last_ai_payloads.setdefault(request.room_id, {})
    room_empty_since.pop(request.room_id, None)
    
    return {"status": "started", "pid": p.pid, "message": f"Bot joining room {request.room_id}"}

async def dispatch_loop(room_id: str):
    try:
        while True:
            queue = active_queues.get(room_id)
            conns = active_connections.get(room_id, {})
            if not conns:
                now = time.time()
                empty_since = room_empty_since.get(room_id)
                if empty_since is None:
                    room_empty_since[room_id] = now
                elif now - empty_since >= WS_EMPTY_GRACE_SEC:
                    logger.info(f"[RoomManager] No WS connections for {WS_EMPTY_GRACE_SEC}s. Terminating bot (room={room_id}).")
                    _terminate_room_bot(room_id)
                    return
                await asyncio.sleep(0.2)
                continue
            room_empty_since.pop(room_id, None)
            if queue:
                try:
                    data = queue.get_nowait()
                except Empty:
                    await asyncio.sleep(0.01)
                    continue
                participant_id = data.get("participantId")
                event_type = data.get("eventType") or data.get("status")
                if participant_id and event_type:
                    room_cache = last_ai_payloads.setdefault(room_id, {})
                    member_cache = room_cache.setdefault(participant_id, {})
                    if event_type == "SCORE" or "score" in data:
                        member_cache["score"] = data
                    else:
                        member_cache["status"] = data
                if participant_id:
                    targets = [(participant_id, conns.get(participant_id))]
                else:
                    targets = list(conns.items())
                for pid, ws in targets:
                    if not ws:
                        continue
                    try:
                        await ws.send_json(data)
                    except Exception as e:
                        logger.warning(f"WS send failed (room={room_id}, member={pid}): {e}")
                        conns.pop(pid, None)
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
        room_empty_since.pop(room_id, None)

        # Send last cached status/score to restore UI state on refresh
        member_cache = last_ai_payloads.get(room_id, {}).get(member_id)
        if member_cache:
            for key in ("status", "score"):
                payload = member_cache.get(key)
                if payload:
                    try:
                        await websocket.send_json(payload)
                    except Exception as e:
                        logger.warning(f"WS send cached failed (room={room_id}, member={member_id}): {e}")

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
            room_empty_since[room_id] = time.time()

@app.get("/health")
async def health_check():
    return {"status": "ok"}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
