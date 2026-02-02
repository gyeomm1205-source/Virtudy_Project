from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from .config import DATABASE_URL

# 엔진은 전역으로 한 번만 생성
engine = create_engine(DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

# 의존성 주입을 위한 제너레이터
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()