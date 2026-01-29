from fastapi import FastAPI
from .routers import avatar
from .database import Base, engine

# 1. 데이터베이스 테이블 생성
# (서버 시작 시 models.py에 정의된 테이블이 없으면 자동으로 만들어줍니다)
Base.metadata.create_all(bind=engine)

# 2. FastAPI 앱 인스턴스 생성 (이게 꼭 있어야 합니다!)
app = FastAPI()

# 3. 라우터 등록
# 이제 /makeAvatar 같은 API 주소를 사용할 수 있게 됩니다.
app.include_router(avatar.router)

# 4. (선택사항) 서버가 잘 켜졌는지 확인하는 테스트용 루트 경로
@app.get("/")
async def root():
    return {"message": "Avatar AI Server is running!"}