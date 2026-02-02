# 1. Base Image
FROM python:3.11-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. [수정됨] 시스템 라이브러리 설치
# Debian 최신 버전(Trixie/Bookworm)에서는 libgl1-mesa-glx가 삭제되었습니다.
# 대신 'libgl1'을 설치해야 합니다.
RUN apt-get update && apt-get install -y \
    libgl1 \
    libglib2.0-0 \
    libxcb1 \
    libx11-xcb1 \
    libxi6 \
    libxrender1 \
    libsm6 \
    libxext6 \
    && rm -rf /var/lib/apt/lists/*

# 4. 의존성 파일 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. 소스 코드 전체 복사
COPY . .

# 6. PYTHONPATH 설정
ENV PYTHONPATH=/app

# 7. 포트 노출
EXPOSE 8000

# 8. 실행 명령어
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]