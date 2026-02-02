# 1. Base Image
FROM python:3.11-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. [핵심] OpenCV 및 GUI 관련 시스템 라이브러리 강제 설치
# 서버에서 GUI 버전의 OpenCV가 돌아가기 위해 필요한 모든 리눅스 파일을 설치합니다.
# 이 부분이 있어야 libxcb 오류가 100% 해결됩니다.
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

# 4. 의존성 파일 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. 소스 코드 전체 복사
COPY . .

# 6. PYTHONPATH 설정 (Import 경로 문제 방지)
ENV PYTHONPATH=/app

# 7. 포트 노출
EXPOSE 8000

# 8. 실행 명령어
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]