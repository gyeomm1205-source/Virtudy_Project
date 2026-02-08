# Virtudy Frontend

## 📖 프로젝트 개요

**Virtudy**의 프론트엔드 저장소입니다.

사용자에게 실시간 화상 스터디 환경을 제공하고, 학습 데이터를 시각화하여 보여주며, 게이미피케이션 요소를 통해 학습 동기를 부여하는 웹 애플리케이션입니다.

Vue 3와 TypeScript를 기반으로 구축되었으며, LiveKit과 Mediapipe를 활용하여 고품질의 화상 통신 및 AI 기반 집중도 분석 기능을 제공합니다.

---

## 🛠 기술 스택 (Tech Stack)

### **Core & Framework**

- **Vue 3**: Composition API 사용
- **TypeScript**: 정적 타입 시스템 적용
- **Vite**: 빠른 개발 서버 및 빌드 도구
- **Pinia**: 상태 관리 (Store)

### **Styling & UI**

- **Tailwind CSS**: 유틸리티 퍼스트 CSS 프레임워크
- **SCSS**: 커스텀 스타일링
- **Chart.js / Vue-chartjs**: 학습 데이터 시각화 (리포트)

### **Real-time & AI**

- **LiveKit Client**: WebRTC 기반 실시간 화상/음성 통신 및 화면 공유
- **Socket.IO / SockJS**: 실시간 채팅 및 상태 동기화
- **Mediapipe (Face Mesh, Hands)**: AI 기반 사용자 졸음 감지 및 제스처 인식

### **HTTP & Utilities**

- **Axios**: HTTP 클라이언트 (Interceptors 활용)
- **Jwt-decode**: JWT 토큰 디코딩

---

## 📂 프로젝트 구조 (Project Structure)

FSD(Feature-Sliced Design)의 개념을 일부 차용하여 기능(Feature) 단위로 디렉토리를 구조화했습니다.

```
src
├── 📂 app              # 애플리케이션 진입점 및 전역 설정
│   ├── 📂 router       # Vue Router 설정
│   ├── 📂 store        # Pinia Store 설정
│   ├── 📂 providers    # 전역 Provider
│   ├── App.vue         # 루트 컴포넌트
│   └── main.ts         # 앱 엔트리 포인트
│
├── 📂 features         # 기능별 모듈 (핵심 비즈니스 로직 및 UI)
│   ├── 📂 auth         # 로그인, 회원가입
│   ├── 📂 study-room   # 화상 스터디 룸 (LiveKit, Chat, Control)
│   ├── 📂 report       # 학습 리포트 및 분석 차트
│   ├── 📂 ranking      # 랭킹 시스템
│   ├── 📂 lobby        # 메인 로비 및 스터디 목록
│   ├── 📂 mypage       # 마이페이지 및 설정
│   ├── 📂 avatar       # 아바타 관련 기능
│   ├── 📂 onboarding   # 신규 유저 온보딩
│   └── 📂 introduction # 서비스 소개 페이지
│
├── 📂 shared           # 공유 모듈 (재사용 가능한 코드)
│   ├── 📂 api          # Axios 인스턴스 및 API 호출 함수
│   ├── 📂 ui           # 공통 UI 컴포넌트 (Button, Input, Modal 등)
│   ├── 📂 types        # 전역 타입 정의
│   ├── 📂 lib          # 외부 라이브러리 랩퍼 등
│   └── 📂 config       # 상수 및 환경 설정
│
└── 📂 assets           # 정적 리소스 (이미지, 폰트, 스타일)
```

---

## ✨ 세부 기능 (Detailed Features)

### 1. 🔐 인증 및 사용자 관리 (Authentication)

- **JWT 기반 인증**: Access Token을 메모리/쿠키에 저장하고, Axios Interceptor를 통해 자동 갱신 및 헤더 주입 처리.
- **소셜 로그인**: Kakao 등 OAuth2 로그인 리다이렉트 처리 및 추가 정보 입력(온보딩) 흐름 구현.

### 2. 📹 실시간 화상 스터디 (Live Study Room)

- **LiveKit 연동**: 다자간 화상 통신, 마이크/카메라 제어, 화면 공유 기능 구현.
- **AI 집중도 분석**: Mediapipe Face Mesh를 활용하여 사용자 눈 깜빡임 및 고개 움직임을 분석, 졸음 감지 시 알림 제공.
- **실시간 소통**: Socket.IO/Stomp를 통한 실시간 채팅 및 스터디 룸 상태 동기화.

### 3. 📊 리포트 및 대시보드 (Report & Dashboard)

- **학습 데이터 시각화**: Chart.js를 사용하여 일간/주간/월간 학습 시간 및 패턴을 그래프로 제공.
- **캘린더 뷰**: 잔디 심기(Github Style) 또는 캘린더 형태로 학습 기록 시각화.

### 4. 🏆 게이미피케이션 (Gamification)

- **랭킹 시스템**: 실시간 랭킹 정보를 받아 순위표 표시.
- **티어 및 아바타**: 사용자 티어에 따른 UI 효과 및 아바타 커스터마이징.

---

## 🚀 시작 가이드 (Getting Started)

### 1. 환경 설정 (Prerequisites)

- Node.js (LTS 버전 권장)
- pnpm (패키지 매니저 권장)

### 2. 설치 (Installation)

```bash
# Frontend 디렉토리로 이동
cd virtudy-frontend

# 패키지 설치
pnpm install
```

### 3. 실행 (Run)

```bash
# 개발 서버 실행
pnpm run dev
```

서버 실행 후 브라우저에서 `http://localhost:5173` 접속
