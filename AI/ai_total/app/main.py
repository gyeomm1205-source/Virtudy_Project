from fastapi import FastAPI
from .routers import avatar
from .database import Base, engine
from fastapi import FastAPI, HTTPException, WebSocket, WebSocketDisconnect
import livekit.api as livekit_api  # 추가
from pydantic import BaseModel
import asyncio
import multiprocessing
import uvicorn
import os
import logging
from run_livekit import run_bot
import json

from fastapi.middleware.cors import CORSMiddleware

# 1. 데이터베이스 테이블 생성
# (서버 시작 시 models.py에 정의된 테이블이 없으면 자동으로 만들어줍니다)
Base.metadata.create_all(bind=engine)

# 2. FastAPI 앱 인스턴스 생성 (이게 꼭 있어야 합니다!)
app = FastAPI()

# 1. 허용할 오리진(도메인) 목록 정의
origins = [
    "http://localhost",
    "http://localhost:3030", # 리액트/뷰 등 프론트엔드 개발 서버
    "https://i14a703.p.ssafy.io",
]

# Initialize Logger
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)


# 2. 미들웨어 추가
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,       # 요청을 허용할 도메인 목록
    allow_credentials=True,      # 쿠키(Cookie) 등 자격 증명 포함 여부
    allow_methods=["*"],         # 허용할 HTTP 메서드 (GET, POST, PUT, DELETE 등)
    allow_headers=["*"],         # 허용할 HTTP 헤더
)

# 3. 라우터 등록
# 이제 /makeAvatar 같은 API 주소를 사용할 수 있게 됩니다.
app.include_router(avatar.router, prefix="/fastapi")
 
# 4. (선택사항) 서버가 잘 켜졌는지 확인하는 테스트용 루트 경로
@app.get("/")
async def root():
    return {"message": "Avatar AI Server is running!"}


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

@app.post("/fastapi/bot/join")
async def join_room(request: JoinRequest):
    """
    Triggers a new AI bot instance to join a LiveKit room.
    """
    logger.info(f"Received request to join room: {request.room_id}")

    # ---------------------------------------------------------
    # [수정] 프론트엔드 토큰 대신, 백엔드에서 봇 전용 토큰 생성
    # ---------------------------------------------------------
    
    # 1. 환경 변수에서 키 가져오기 (이미 도커 환경변수에 설정되어 있다고 가정)
    API_KEY = os.getenv("LIVEKIT_API_KEY") 
    API_SECRET = os.getenv("LIVEKIT_API_SECRET")

    # (혹시 환경변수 키 이름이 다르면 아래에 직접 입력해서 테스트해보세요)
    # API_KEY = "devkey" 
    # API_SECRET = "secret"

    if not API_KEY or not API_SECRET:
        logger.error("LiveKit API Key/Secret not found in env variables!")
        return {"status": "error", "message": "Server API Key missing"}

    # 2. 봇 전용 Identity 생성 (유저와 겹치지 않게 "bot_" 접두사 붙임)
    bot_identity = f"bot_{request.room_id}" 

    # 3. 토큰 발급
    grant = livekit_api.VideoGrants(room_join=True, room=request.room_id)
    bot_token = livekit_api.AccessToken(API_KEY, API_SECRET) \
        .with_identity(bot_identity) \
        .with_name("AI Analysis Bot") \
        .with_grants(grant) \
        .to_jwt()

    # ---------------------------------------------------------
    
    # logger.info(f"REQUEST : ROOM ID : {request.room_id}, QUEUE: {queue}")
    # Create a shared queue for this room using standard multiprocessing.Queue
    # (Since we are forking/spawning from this process, it works)
    queue = multiprocessing.Queue()
    active_queues[request.room_id] = queue

    # Spawn a new process for the bot so it doesn't block the API server
    p = multiprocessing.Process(target=bot_process, args=(request.url, bot_token, queue))
    p.start()
    
    return {"status": "started", "pid": p.pid, "message": f"Bot joining room {bot_identity}"}

@app.websocket("/fastapi/ws/analysis/{room_id}/{member_id}")
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