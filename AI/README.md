# **Virtudy AI & Avatar Service**

**Virtudy**의 AI 서비스 저장소입니다.

사용자의 학습 집중도를 실시간으로 분석하는 **프론트엔드 로컬 AI(On-Device AI)**와, 사용자 사진을 분석하여 아바타를 생성해주는 **백엔드 AI 서버(FastAPI)**로 구성되어 있습니다.

기존 서버 기반의 비디오 분석 로직을 클라이언트(브라우저)로 이관하여 서버 부하를 최소화하고, 사용자의 개인정보(비디오 스트림) 보호를 강화하였습니다.

---

## **🛠 기술 스택 (Tech Stack)**

### **Frontend AI (Local Inference)**

- **TensorFlow.js**: 브라우저 기반 딥러닝 추론 엔진 (WebGL/WebGPU 가속)
- **MediaPipe Tasks**:
    - **Face Mesh**: 478개의 얼굴 랜드마크 추출 (졸음, 시선, 부재 감지)
    - **Hands**: 손 제스처 및 위치 추적
- **YOLOv11 (TFJS)**: 객체 탐지 (스마트폰 사용 감지)
- **TypeScript**: 타입 안정성이 보장된 AI 로직 구현

### **Backend AI (Avatar Analysis)**

- **Python**: 3.10+
- **FastAPI**: 고성능 비동기 Python 웹 프레임워크
- **OpenAI GPT-4o-mini (Vision)**: 사용자 사진 분석 및 아바타 파츠(Hair, Eyes, Outfit 등) 매칭
- **SQLAlchemy**: DB 연동 (MySQL)

### **Backend AI (Weekly Report Analysis)**

- **OpenAI GPT-4o:** 사용자 주간 학습 데이터 분석 및 진단 → 개선 목표 제안

---

## **📂 프로젝트 구조 (Package Structure)**

AI 관련 코드는 프론트엔드(실시간 감지)와 백엔드(아바타 생성)로 나뉘어 있습니다.

### **1. Frontend AI Structure (`FE/virtudy-frontend`)**

`src/features/study-room/logic`에 핵심 로직이 집중되어 있습니다.

```tsx
src/features/study-room/logic
├── 📄 localAiEngine.ts           # AI 상태 머신 (Focus, Sleep, Phone, Away 판단)
├── 📄 localAiFeatureExtractor.ts # MediaPipe & YOLO 모델 로드 및 추론 실행
├── 📄 useLocalAiRunner.ts        # Reactivity Hook (Vue.js) - 비디오 스트림 연결
├── 📄 useAiStore.ts              # 상태 관리 (Pinia) - 집중도 점수 계산 및 저장
└── 📄 scoreUtils.ts              # 집중도 점수 산식 유틸리티
```

- **Assets**:
    - `public/mediapipe/`: FaceMesh, Hands 모델 자산 (WASM)
    - `public/models/yolo11/`: YOLO 객체 탐지 모델 (`model.json`, `.bin`)

### **2. Backend AI Structure (`AI/ai_total`)**

FastAPI 기반의 아바타 분석 서버입니다.

```python
AI/ai_total
├── 📂 app
│   ├── 📄 main.py                # FastAPI 앱 엔트리포인트
│   ├── 📂 routers
│   │   └── 📄 avatar.py          # 아바타 생성 API (POST /fastapi/avatar)
│   ├── 📂 services
│   │   ├── 📄 gpt_service.py     # GPT Vision API 연동 (이미지 -> 파츠 추천)
│   │   └── 📄 image_service.py   # 이미지 전처리 (Resizing, Base64 변환)
│   └── 📄 database.py            # DB 세션 관리
├── 📄 requirements.txt           # 의존성 패키지 목록
└── 📄 Dockerfile                 # 서버 배포용 Docker 설정
```

### 3. Backend AI Structure (`BE/`)

사용자의 주간 학습 데이터 분석 및 진단 로직이 집중되어 있습니다.

```jsx
BE
├── 📄 build.gradle               # WebFlux(WebClient), Lombok 의존성 관리
├── 📂 src
│   └── 📂 main
│       ├── 📂 java
│       │   └── 📂 com
│       │       └── 📂 ssafy
│       │           └── 📂 virtudy
│       │               └── 📂 report
│       │                   └── 📂 service
│       │                       └── 📄 AiAnalysisService.java  # GMS(GPT-4o) API 연동, 학습 분석 피드백 생성
│       └── 📂 resources
│           └── 📄 application.yml       # GMS API Key 설정 (gms.key)
```

---

## **✨ 세부 기능 (Detailed Features)**

### **1. 👁️ 실시간 집중도 모니터링 (Frontend Local AI)**

브라우저에서 직접 추론을 수행하므로 비디오 데이터가 서버로 전송되지 않습니다.

- **졸음 감지 (Drowsiness)**:
    - **EAR (Eye Aspect Ratio)**: 눈이 일정 시간 이상 감겨있는지 분석.
    - **Head Nodding**: 고개가 급격히 떨어지는지(Pitch) 감지.
- **스마트폰 사용 감지 (Phone Usage)**:
    - **YOLO Object Detection**: 화면 내 스마트폰 객체(Class 67) 탐지.
    - **Hand Tracking**: 손이 얼굴 근처로 올라오거나 폰을 쥔 동작 보조 감지.
- **자리 비움 (Absence)**:
    - 얼굴 랜드마크가 일정 시간(Grace Period) 동안 검출되지 않을 경우 감지.
- **집중도 점수 (Concentration Score)**:
    - 위 상태들을 종합하여 100점 만점 기준으로 실시간 점수 차감/회복.

### **2. 🎨 AI 아바타 생성 (Backend AI)**

- **GPT Vision 분석**: 사용자가 업로드한 사진을 분석하여 얼굴형, 헤어 스타일, 눈 모양, 안경 착용 여부, 옷 색상 등을 추출합니다.
- **파츠 매칭 (Mapping)**: 프론트엔드에 존재하는 아바타 파츠 에셋(SVG) 중 가장 유사한 조합을 추천하여 DB에 저장합니다.

### 3. 💬 AI 코멘트 생성 (Backend AI)

- **GPT 데이터 분석:** 사용자의 주간 학습 데이터를 분석하여 4가지 관점으로 진단 및 조언을 제시합니다.
    - 진단: 지난주 학습 상태 요약
    - 비용: 학습 중 집중 저하로 발생한 기회비용
    - 목표: 이번주 학습을 위한 개선점
    - 온도: 지난주에 대한 총평을 직관적으로 제시

---

## **🚀 시작 가이드 (Getting Started)**

### **1. Frontend AI (Watch Mode)**

프론트엔드 프로젝트에 통합되어 있으므로, FE 서버를 실행하면 자동으로 로드됩니다.

```bash
cd FE/virtudy-frontend
pnpm install
pnpm dev
# 브라우저에서 스터디룸 입장 시 모델 자동 다운로드 및 추론 시작
```

### **2. Backend AI Server**

아바타 생성 기능을 위해 별도로 실행해야 합니다.

```bash
cd AI/ai_total

# 가상환경 생성 및 활성화
python -m venv venv
# Windows:
.\venv\Scripts\activate
# Mac/Linux:
source venv/bin/activate

# 의존성 설치
pip install -r requirements.txt

# 서버 실행 (Port: 8000)
uvicorn app.main:app --reload
```

### **3. 환경 변수 설정 (.env)**

### **Backend (`AI/ai_total/.env`)**

```
# OpenAI (Required for Avatar Analysis)
OPENAI_API_KEY=sk-...

# Database (MySQL)
# Format: mysql+pymysql://<username>:<password>@<host>:<port>/<dbname>
SQLALCHEMY_DATABASE_URL=mysql+pymysql://root:password@localhost:3306/virtudy

# LiveKit (Server Token Generation)
LIVEKIT_API_KEY=devkey
LIVEKIT_API_SECRET=secret

# Kafka (Event Publishing) (Optional)
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### **Frontend (`FE/virtudy-frontend/.env`)**

로컬 AI 및 서비스 연동을 위한 환경 변수입니다.

```
# API Base URL
VITE_API_BASE_URL=https://i14a703.p.ssafy.io/api

# LiveKit & Socket
VITE_LIVEKIT_URL=wss://i14a703.p.ssafy.io/livekit
VITE_SOCKET_URL=https://i14a703.p.ssafy.io/ws

# AI Server (Avatar)
VITE_AI_API_URL=https://i14a703.p.ssafy.io/fastapi

# Local AI Configuration
VITE_LOCAL_PHONE_MODEL_URL=/models/yolo11/model.json
VITE_MEDIAPIPE_BASE_URL=/mediapipe
# VITE_LOCAL_AI_DEBUG=true  # 디버그 모드 활성화 시

# OAuth (Kakao)
VITE_KAKAO_CLIENT_ID=your_kakao_client_id
VITE_KAKAO_REDIRECT_URI=http://localhost:3030/auth/callback
```

### Backend (`BE/src/main/resources/application-dev.yaml`)

주간 리포트의 AI 코멘트 생성을 위한 환경 변수입니다.

```jsx
# GMS (Required for AI Feedback)
GMS_KEY=sk-...  # SSAFY OpenAI Proxy API Key

# Database (MySQL)
DEV_DB_HOST=localhost
DEV_DB_PORT=3306
DEV_DB_NAME=virtudy
DEV_DB_USER=root
DEV_DB_PASSWORD=password

# Redis
REDIS_PORT=6379
DEV_REDIS_PASSWORD=password

# Kafka (Event Publishing)
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# LiveKit
LIVEKIT_URL=wss://...
LIVEKIT_API_KEY=devkey
LIVEKIT_API_SECRET=secret
```