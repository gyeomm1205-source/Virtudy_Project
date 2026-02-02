from fastapi import FastAPI
from .routers import avatar
from .database import Base, engine
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