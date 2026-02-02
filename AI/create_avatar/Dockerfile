# 1. Python 3.11 slim 버전 사용 (가볍고 안정적)
FROM python:3.11-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. [중요] 시스템 의존성 설치 (주석 해제 및 보강됨)
# 현재 발생 중인 'libxcb.so.1' 에러와 'cv2' 관련 에러를 해결하기 위한 필수 패키지들입니다.
# 절대 주석 처리하지 마세요.
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

# 6. PYTHONPATH 설정 (ModuleNotFoundError 방지)
# 파이썬이 /app 디렉토리를 모듈 검색 경로로 인식하게 합니다.
ENV PYTHONPATH=/app

# 7. 포트 노출
EXPOSE 8000

# 8. 실행 명령어
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]