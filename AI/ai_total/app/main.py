from fastapi import FastAPI
from .routers import avatar
from .database import Base, engine
from fastapi import FastAPI, HTTPException, WebSocket, WebSocketDisconnect, APIRouter
from pydantic import BaseModel
import asyncio
import multiprocessing
import uvicorn
import logging
from run_livekit import run_bot
import json

from fastapi.middleware.cors import CORSMiddleware


main_router = APIRouter()

# 1. 데이터베이스 테이블 생성
# (서버 시작 시 models.py에 정의된 테이블이 없으면 자동으로 만들어줍니다)
Base.metadata.create_all(bind=engine)

# 2. FastAPI 앱 인스턴스 생성 (이게 꼭 있어야 합니다!)
app = FastAPI()

# Initialize Logger
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)


# 1. 허용할 오리진(도메인) 목록 정의
origins = [
    "http://localhost",
    "http://localhost:3030", # 리액트/뷰 등 프론트엔드 개발 서버
    "https://i14a703.p.ssafy.io",
]

# 2. 미들웨어 추가
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,       # 요청을 허용할 도메인 목록
    allow_credentials=True,      # 쿠키(Cookie) 등 자격 증명 포함 여부
    allow_methods=["*"],         # 허용할 HTTP 메서드 (GET, POST, PUT, DELETE 등)
    allow_headers=["*"],         # 허용할 HTTP 헤더
)

# [DEBUG] Middleware to print headers
class HeaderPrinterMiddleware:
    def __init__(self, app):
        self.app = app

    async def __call__(self, scope, receive, send):
        if scope["type"] == "websocket":
            headers = dict(scope.get("headers", []))
            print(f"\n[DEBUG] WebSocket Connection Attempt: {scope['path']}")
            print("[DEBUG] Received Headers:")
            for k, v in headers.items():
                try:
                    key = k.decode()
                    value = v.decode()
                    print(f"  {key}: {value}")
                except:
                    print(f"  {k}: {v}")
            print("-" * 30)
        await self.app(scope, receive, send)

app.add_middleware(HeaderPrinterMiddleware)

# 3. 라우터 등록
# 이제 /makeAvatar 같은 API 주소를 사용할 수 있게 됩니다.
app.include_router(avatar.router, prefix="/fastapi")

app.include_router(main_router, prefix="/fastapi")
 
# 4. (선택사항) 서버가 잘 켜졌는지 확인하는 테스트용 루트 경로
@main_router.get("/")
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

@main_router.post("/bot/join")
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

@main_router.websocket("/ws/analysis/{room_id}/{member_id}")
async def websocket_endpoint(websocket: WebSocket, room_id: str, member_id: str):
    """
    Handles WebSocket connections from the frontend.
    """

    # 1. 먼저 로그 출력 (이미 잘 하고 계심)
    print(f"[DEBUG] WebSocket Connection Attempt: {room_id}")

    await websocket.accept()
    logger.info(f"WebSocket connected: Room {room_id}, Member {member_id}")
    
    # 3. 토큰 검사 (쿠키 확인)
    # Authorization 헤더는 없을 확률이 높으니 쿠키를 우선 확인하세요.
    cookie_token = websocket.cookies.get("accessToken") # 혹은 refreshToken

    if not cookie_token:
        print("[ERROR] 토큰이 없습니다. 연결 종료.")
        await websocket.close(code=1008) # 1008: 정책 위반
        cookie_token = websocket.cookies.get("refreshToken")
        return
    
    # 두 토큰 다 없으면 에러 처리
    if not cookie_token:
        print(f"[ERROR] 토큰(Access/Refresh)이 모두 없습니다. 쿠키: {websocket.cookies.keys()}")
        await websocket.close(code=1008) 
        return

    # Try to get queue, but don't give up if missing (bot might join later)
    queue = active_queues.get(room_id)
    if not queue:
        logger.warning(f"No active AI bot found for room {room_id}. Waiting for bot to join...")
    else:
        logger.info(f"Queue found for room {room_id}. Listening for data...")
    
    try:
        while True:
            # If queue is missing, try to find it again
            if queue is None:
                queue = active_queues.get(room_id)
                if queue:
                    logger.info(f"Bot joined! Queue found for room {room_id}.")
                else:
                    # Still no queue, wait a bit and retry
                    await asyncio.sleep(1)
                    continue

            # Queue exists, check for data
            if not queue.empty():
                try:
                    data = queue.get_nowait()
                    # logger.info(f"Sending data to WS: {data}")
                    print(f"[DEBUG] Sending to FE: {data['value']} ({data['eventType']})")
                    await websocket.send_json(data)
                except multiprocessing.queues.Empty:
                    pass
            else:
                await asyncio.sleep(0.01) # Faster polling

    except WebSocketDisconnect:
        logger.info(f"WebSocket disconnected: Room {room_id}, Member {member_id}")
    except Exception as e:
        logger.error(f"WebSocket error: {e}")

@main_router.get("/health")
async def health_check():
    return {"status": "ok"}