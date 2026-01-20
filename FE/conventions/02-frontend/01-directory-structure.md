# 디렉토리 구조 가이드

> **목적**: VIRTUDY 프로젝트의 Feature-Sliced Design (FSD) 기반 디렉토리 구조 이해 및 활용

---

## 📂 1. 전체 프로젝트 구조 (FSD)

```
virtudy/
├── src/
│   ├── app/                        # 애플리케이션 진입점 및 전역 설정
│   │   ├── App.vue                # 루트 컴포넌트
│   │   ├── main.ts                # 애플리케이션 초기화
│   │   ├── router/                # 라우터 설정
│   │   │   └── index.ts
│   │   ├── store/                 # Pinia 스토어 설정
│   │   │   └── index.ts
│   │   └── providers/             # 🆕 Context Providers
│   │       └── HighFrequencyProvider.vue  # 고속 데이터 제공
│   │
│   ├── features/                   # 🎯 핵심! 기능별 모듈 (Feature-Sliced)
│   │   ├── study-room/            # 스터디룸 기능
│   │   │   ├── ui/                # UI 컴포넌트 (B: UI 담당자)
│   │   │   │   ├── StudyRoom.vue
│   │   │   │   ├── Avatar.vue
│   │   │   │   ├── VideoPanel.vue
│   │   │   │   ├── TimerPanel.vue
│   │   │   │   ├── FocusChart.vue
│   │   │   │   └── ControlBar.vue
│   │   │   │
│   │   │   ├── logic/             # 비즈니스 로직 (A: AI, C: Logic)
│   │   │   │   ├── useFocus.ts    # 🆕 집중도 분석 (A)
│   │   │   │   ├── useAvatar.ts   # 🆕 아바타 매핑 (A)
│   │   │   │   ├── useWebRTC.ts   # WebRTC 연결 (C)
│   │   │   │   ├── useSocket.ts   # Socket 통신 (C)
│   │   │   │   └── useTimer.ts    # 타이머 로직 (C)
│   │   │   │
│   │   │   ├── api/               # API 통신 (C: Backend)
│   │   │   │   ├── roomAPI.ts
│   │   │   │   └── studyAPI.ts
│   │   │   │
│   │   │   ├── types/             # 타입 정의 (공통)
│   │   │   │   ├── room.types.ts
│   │   │   │   └── focus.types.ts
│   │   │   │
│   │   │   └── index.ts           # Public API (외부 노출)
│   │   │
│   │   ├── auth/                  # 인증 기능
│   │   │   ├── ui/
│   │   │   │   ├── LoginForm.vue
│   │   │   │   └── SignupForm.vue
│   │   │   ├── logic/
│   │   │   │   └── useAuth.ts
│   │   │   ├── api/
│   │   │   │   └── authAPI.ts
│   │   │   └── types/
│   │   │       └── auth.types.ts
│   │   │
│   │   ├── lobby/                 # 로비 (방 목록)
│   │   ├── ranking/               # 랭킹
│   │   ├── report/                # 리포트
│   │   └── mypage/                # 마이페이지
│   │
│   ├── shared/                    # 공유 리소스 (모든 feature에서 사용)
│   │   ├── ui/                    # 공통 UI 컴포넌트
│   │   │   ├── Button.vue
│   │   │   ├── Modal.vue
│   │   │   ├── Input.vue
│   │   │   └── LoadingSpinner.vue
│   │   │
│   │   ├── lib/                   # 유틸리티 함수
│   │   │   ├── format.ts          # 날짜/시간 포맷
│   │   │   ├── validation.ts      # 유효성 검증
│   │   │   └── storage.ts         # LocalStorage 래퍼
│   │   │
│   │   ├── api/                   # 공통 API 설정
│   │   │   ├── axios.config.ts    # Axios 인스턴스
│   │   │   └── interceptors.ts    # 요청/응답 인터셉터
│   │   │
│   │   └── config/                # 환경 설정
│   │       ├── env.ts             # 환경 변수
│   │       └── constants.ts       # 상수
│   │
│   ├── entities/                  # 비즈니스 엔티티 (도메인 모델)
│   │   ├── user/
│   │   │   ├── model/
│   │   │   │   └── userStore.ts   # Pinia 사용자 스토어
│   │   │   ├── api/
│   │   │   │   └── userAPI.ts
│   │   │   └── types/
│   │   │       └── user.types.ts
│   │   │
│   │   ├── room/
│   │   │   ├── model/
│   │   │   │   └── roomStore.ts   # Pinia 방 스토어
│   │   │   └── types/
│   │   │       └── room.types.ts
│   │   │
│   │   └── ranking/
│   │       ├── model/
│   │       │   └── rankingStore.ts
│   │       └── types/
│   │           └── ranking.types.ts
│   │
│   ├── core/                      # 🆕 핵심 인프라 (Singleton 매니저)
│   │   ├── managers/              # 생명주기 관리 매니저
│   │   │   ├── SocketManager.ts   # 🔌 Socket.IO 매니저
│   │   │   ├── WebRTCManager.ts   # 📹 WebRTC 매니저
│   │   │   └── MediaPipeManager.ts # 🤖 MediaPipe 매니저
│   │   │
│   │   └── context/               # 🆕 고속 데이터 Context
│   │       └── HighFrequencyContext.ts  # 60FPS 데이터 바이패스
│   │
│   └── assets/                    # 정적 자산
│       ├── images/
│       ├── styles/
│       │   ├── global.css
│       │   └── neon.css           # 80년대 네온 스타일
│       └── fonts/
│
├── public/                        # 빌드 시 복사되는 정적 파일
│   └── favicon.ico
│
├── conventions/                   # 📘 프로젝트 컨벤션 문서
│   ├── README.md
│   ├── 01-git/
│   │   ├── 01-branch-strategy.md
│   │   ├── 02-commit-message.md
│   │   └── 03-pull-request-rules.md
│   └── 02-frontend/
│       ├── 01-directory-structure.md
│       ├── 02-naming.md
│       ├── 03-coding-style.md
│       ├── 04-state-management.md
│       └── 05-testing.md
│
├── tests/                         # 테스트 코드
│   ├── unit/
│   ├── integration/
│   └── e2e/
│
├── .vscode/                       # VSCode 설정
├── .husky/                        # Git hooks
├── node_modules/
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

---

## 🎯 2. Feature-Sliced Design (FSD) 핵심 원칙

### 2.1 Layer 계층 구조

```
┌─────────────────────────────────────────┐
│  app/         애플리케이션 진입점       │
├─────────────────────────────────────────┤
│  features/    기능별 모듈 (핵심!)       │
├─────────────────────────────────────────┤
│  entities/    비즈니스 엔티티           │
├─────────────────────────────────────────┤
│  shared/      공유 리소스               │
└─────────────────────────────────────────┘
```

### 2.2 Feature 내부 구조 (Slices)

각 feature는 **ui**, **logic**, **api**, **types**로 구성:

```
features/study-room/
├── ui/        # 🎨 UI 컴포넌트 (B: UI 담당)
├── logic/     # 🧠 비즈니스 로직 (A: AI, C: Logic)
├── api/       # 🌐 API 통신 (C: Backend)
├── types/     # 📝 타입 정의 (공통)
└── index.ts   # 📦 Public API
```

---

## 🧩 3. 역할별 작업 영역 (협업 가이드)

### 👨‍🎨 B (UI 담당자)
**작업 영역**: `features/*/ui/` **만**
```
features/study-room/ui/
├── StudyRoom.vue       # 메인 페이지
├── Avatar.vue          # 아바타 컴포넌트
├── VideoPanel.vue      # 비디오 패널
└── TimerPanel.vue      # 타이머 패널
```

**작업 예시**:
- 컴포넌트 레이아웃 구현
- CSS 스타일링 (80년대 네온 디자인)
- 반응형 UI 처리
- 애니메이션 효과

### 🤖 A (AI 담당자)
**작업 영역**: `features/*/logic/useFocus.ts`, `useAvatar.ts`
```
features/study-room/logic/
├── useFocus.ts         # 집중도 분석
└── useAvatar.ts        # 아바타 매핑
```

**작업 예시**:
- MediaPipe 데이터 처리
- 집중도 알고리즘 구현
- 얼굴 좌표 → 아바타 변환

### 🔌 C (Logic/Backend 담당자)
**작업 영역**: `features/*/logic/` (WebRTC, Socket), `features/*/api/`
```
features/study-room/
├── logic/
│   ├── useWebRTC.ts    # WebRTC 연결
│   ├── useSocket.ts    # Socket 통신
│   └── useTimer.ts     # 타이머 로직
└── api/
    ├── roomAPI.ts      # 방 API
    └── studyAPI.ts     # 학습 기록 API
```

**작업 예시**:
- WebRTC 연결/해제 로직
- Socket.IO 이벤트 처리
- REST API 호출
- 타이머/비즈니스 로직

---

## 📋 4. 파일 배치 규칙

### 4.1 UI 컴포넌트

#### ✅ 올바른 배치
```
features/study-room/ui/
├── StudyRoom.vue           # 페이지 컴포넌트
├── Avatar.vue              # Feature 전용 컴포넌트
└── TimerPanel.vue

shared/ui/
├── Button.vue              # 여러 feature에서 사용
├── Modal.vue
└── Input.vue
```

#### ❌ 잘못된 배치
```
❌ features/study-room/ui/Button.vue     # 공통 컴포넌트는 shared/ui/
❌ shared/ui/StudyRoomAvatar.vue         # feature 전용은 features/*/ui/
```

### 4.2 비즈니스 로직

#### ✅ 올바른 배치
```
features/study-room/logic/
└── useFocus.ts              # 스터디룸 전용 로직

shared/lib/
└── format.ts                # 공통 유틸리티
```

#### ❌ 잘못된 배치
```
❌ shared/lib/useFocus.ts    # feature 전용 로직
❌ features/study-room/logic/formatDate.ts  # 공통 유틸은 shared/lib/
```

### 4.3 API 통신

#### ✅ 올바른 배치
```
features/study-room/api/
└── roomAPI.ts               # 스터디룸 API

shared/api/
├── axios.config.ts          # Axios 설정
└── interceptors.ts          # 인터셉터
```

---

## 🔍 5. 파일 명명 규칙

### 5.1 Vue 컴포넌트
```
PascalCase.vue

✅ StudyRoom.vue
✅ Avatar.vue
✅ TimerPanel.vue
❌ study-room.vue
❌ avatar.vue
```

### 5.2 Composables (로직)
```
camelCase.ts, use로 시작

✅ useFocus.ts
✅ useWebRTC.ts
✅ useTimer.ts
❌ focus.ts
❌ UseFocus.ts
```

### 5.3 API 파일
```
camelCase.ts, API로 끝남

✅ roomAPI.ts
✅ authAPI.ts
✅ studyAPI.ts
❌ room-api.ts
❌ RoomAPI.ts
```

### 5.4 타입 정의
```
camelCase.types.ts

✅ room.types.ts
✅ focus.types.ts
✅ user.types.ts
❌ roomTypes.ts
❌ room-types.ts
```

### 5.5 Pinia Store
```
camelCaseStore.ts

✅ userStore.ts
✅ roomStore.ts
✅ rankingStore.ts
❌ UserStore.ts
❌ user-store.ts
```

---

## 🚀 6. 실전 예시: 새 기능 추가

### 시나리오: "채팅 기능" 추가

#### Step 1: Feature 폴더 생성
```bash
mkdir -p src/features/chat/{ui,logic,api,types}
```

#### Step 2: 파일 생성
```
features/chat/
├── ui/
│   ├── ChatPanel.vue           # B (UI 담당)
│   ├── ChatMessage.vue
│   └── ChatInput.vue
├── logic/
│   ├── useChat.ts              # C (Logic 담당)
│   └── useChatSocket.ts
├── api/
│   └── chatAPI.ts              # C (Backend 담당)
├── types/
│   └── chat.types.ts           # 공통
└── index.ts                     # Public API
```

#### Step 3: index.ts 작성 (외부 노출)
```typescript
// features/chat/index.ts
export { default as ChatPanel } from './ui/ChatPanel.vue'
export { useChat } from './logic/useChat'
export type { Message, ChatRoom } from './types/chat.types'
```

#### Step 4: 다른 feature에서 사용
```vue
<!-- features/study-room/ui/StudyRoom.vue -->
<script setup lang="ts">
import { ChatPanel } from '@/features/chat'  // ✅ index.ts를 통해 import
</script>

<template>
  <ChatPanel />
</template>
```

---

## 📌 7. 핵심 규칙 요약

### ✅ DO (해야 할 것)
1. **Feature는 독립적으로 유지**: 다른 feature에 직접 의존 ❌
2. **역할별 폴더 분리**: ui, logic, api, types
3. **Public API 노출**: index.ts를 통해 외부 노출
4. **공통 리소스는 shared/**: 여러 feature에서 사용하는 것

### ❌ DON'T (하지 말아야 할 것)
1. **Feature 간 직접 import ❌**
   ```typescript
   // ❌ 나쁜 예
   import { useTimer } from '@/features/study-room/logic/useTimer'
   
   // ✅ 좋은 예: shared로 이동하거나 index.ts를 통해 import
   import { useTimer } from '@/features/study-room'
   ```

2. **UI 컴포넌트에 비즈니스 로직 작성 ❌**
   ```vue
   <!-- ❌ 나쁜 예 -->
   <script setup>
   const analyzeFocus = () => {
     // 복잡한 집중도 분석 로직...
   }
   </script>
   
   <!-- ✅ 좋은 예 -->
   <script setup>
   import { useFocus } from '../logic/useFocus'
   const { analyzeFocus } = useFocus()
   </script>
   ```

3. **순환 의존성 생성 ❌**
   ```
   ❌ features/chat → features/study-room → features/chat
   ```

---

## 🎯 8. 마이그레이션 가이드 (기존 구조 → FSD)

### Before (기존 구조)
```
src/
├── components/
│   ├── StudyRoom.vue
│   ├── Avatar.vue
│   └── ChatPanel.vue
├── composables/
│   ├── useFocus.ts
│   └── useChat.ts
└── services/
    └── api.ts
```

### After (FSD 구조)
```
src/
├── features/
│   ├── study-room/
│   │   ├── ui/StudyRoom.vue
│   │   ├── ui/Avatar.vue
│   │   └── logic/useFocus.ts
│   └── chat/
│       ├── ui/ChatPanel.vue
│       └── logic/useChat.ts
└── shared/
    └── api/axios.config.ts
```

---

## 📚 참고 자료

- [Feature-Sliced Design 공식 문서](https://feature-sliced.design/)
- [VIRTUDY FRONTEND_ARCHITECTURE_V3_FINAL.md](../FRONTEND_ARCHITECTURE_V3_FINAL.md)
- [Vue 3 공식 스타일 가이드](https://vuejs.org/style-guide/)

---

**목표**: 🎯 **협업 충돌 90% 감소, 코드 재사용성 향상, 유지보수 용이**