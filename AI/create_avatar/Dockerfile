# 1. Base Image
FROM python:3.11-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. [핵심] OpenCV 및 GUI 관련 시스템 라이브러리 일괄 설치
# libxcb, libgl 등 OpenCV 실행에 필수적인 리눅스 패키지를 모두 설치합니다.
RUN apt-get update && apt-get install -y \
    libgl1-mesa-glx \
    libglib2.0-0 \
    libxcb1 \
    libx11-xcb1 \
    libxi6 \
    libxrender1 \
    libsm6 \
    libxext6 \
    && rm -rf /var/lib/apt/lists/*

# 4. 의존성 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. 소스 코드 복사
COPY . .

# 6. PYTHONPATH 설정
ENV PYTHONPATH=/app

# 7. 실행
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]