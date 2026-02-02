# 1. Python 3.9 slim 버전 사용 (가볍고 안정적)
# 필요에 따라 3.10이나 3.11로 변경 가능
FROM python:3.11-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. (옵션) 이미지 처리 라이브러리(OpenCV 등) 사용 시 필요한 시스템 패키지 설치
# AI/아바타 관련이라 OpenCV 등을 쓴다면 주석을 해제하세요.
# RUN apt-get update && apt-get install -y \
#     libgl1-mesa-glx \
#     libglib2.0-0 \
#     && rm -rf /var/lib/apt/lists/*

# 4. 의존성 파일 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. 소스 코드 전체 복사
COPY . .

# 6. 포트 노출 (문서화 용도)
EXPOSE 8000

# 7. 실행 명령어 (FastAPI 서버 시작)
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]