# 네이밍 규칙 (Naming Conventions)

> **목적**: 일관성 있고 읽기 쉬운 코드 작성을 위한 네이밍 가이드

---

## 📂 1. 파일 및 폴더 네이밍

### 1.1 Vue 컴포넌트 파일
```
PascalCase.vue

✅ StudyRoom.vue
✅ Avatar.vue
✅ TimerPanel.vue
✅ ChatMessage.vue
❌ study-room.vue
❌ avatar.vue
❌ timer_panel.vue
```

**규칙**:
- **PascalCase** 사용 (각 단어의 첫 글자 대문자)
- **명사형** 사용 (컴포넌트는 "사물"을 나타냄)
- **2개 이상의 단어** 권장 (충돌 방지)

### 1.2 TypeScript/JavaScript 파일

#### Composables (로직)
```
camelCase.ts, use로 시작

✅ useFocus.ts
✅ useWebRTC.ts
✅ useTimer.ts
✅ useAuth.ts
❌ focus.ts
❌ UseFocus.ts
❌ use-focus.ts
```

#### API 파일
```
camelCase.ts, API로 끝남

✅ roomAPI.ts
✅ authAPI.ts
✅ studyAPI.ts
✅ chatAPI.ts
❌ room-api.ts
❌ RoomAPI.ts
❌ roomApi.ts
```

#### 유틸리티 파일
```
camelCase.ts

✅ format.ts
✅ validation.ts
✅ storage.ts
✅ helpers.ts
❌ Format.ts
❌ format-utils.ts
```

#### 타입 정의 파일
```
camelCase.types.ts

✅ room.types.ts
✅ focus.types.ts
✅ user.types.ts
✅ chat.types.ts
❌ roomTypes.ts
❌ room-types.ts
❌ RoomTypes.ts
```

#### Pinia Store 파일
```
camelCaseStore.ts

✅ userStore.ts
✅ roomStore.ts
✅ rankingStore.ts
✅ chatStore.ts
❌ UserStore.ts
❌ user-store.ts
```

### 1.3 폴더 네이밍
```
kebab-case (소문자 + 하이픈)

✅ study-room/
✅ user-profile/
✅ chat-panel/
✅ video-call/
❌ StudyRoom/
❌ study_room/
❌ studyRoom/
```

### 1.4 CSS/스타일 파일
```
kebab-case.css

✅ global.css
✅ neon-theme.css
✅ study-room.css
❌ Global.css
❌ neonTheme.css
❌ study_room.css
```

---

## 🎨 2. Vue 컴포넌트 네이밍

### 2.1 컴포넌트 분류별 네이밍

#### 페이지 컴포넌트
```vue
<!-- ✅ ~Page로 끝남 -->
LoginPage.vue
SignupPage.vue
StudyRoomPage.vue
RankingPage.vue
MyPage.vue

<!-- ❌ 나쁜 예 -->
Login.vue           # 너무 일반적
StudyRoom.vue       # 페이지인지 컴포넌트인지 불분명
```

#### Feature 전용 컴포넌트
```vue
<!-- ✅ Feature 도메인 포함 -->
StudyRoomAvatar.vue
StudyRoomTimer.vue
ChatPanel.vue
ChatMessage.vue

<!-- ❌ 나쁜 예 -->
Avatar.vue          # 너무 일반적 (shared/ui와 혼동)
Timer.vue           # 어느 feature인지 불분명
```

#### 공통 컴포넌트 (shared/ui)
```vue
<!-- ✅ 일반적이고 재사용 가능한 이름 -->
Button.vue
Modal.vue
Input.vue
Card.vue
LoadingSpinner.vue

<!-- ❌ 나쁜 예 -->
StudyButton.vue     # feature 전용이면 features/*/ui/로 이동
TheButton.vue       # The는 싱글톤 컴포넌트에만 사용
```

#### 레이아웃 컴포넌트
```vue
<!-- ✅ Layout으로 끝남 -->
DefaultLayout.vue
AuthLayout.vue
AdminLayout.vue

<!-- ❌ 나쁜 예 -->
Layout.vue          # 너무 일반적
MainLayout.vue      # Main은 모호함
```

#### 싱글톤 컴포넌트 (앱에서 한 번만 사용)
```vue
<!-- ✅ The로 시작 -->
TheHeader.vue
TheFooter.vue
TheSidebar.vue

<!-- ❌ 나쁜 예 -->
Header.vue          # The 없으면 재사용 가능한 것으로 오해
AppHeader.vue       # The 사용 권장
```

### 2.2 컴포넌트 이름 길이

#### ✅ 좋은 예 (2-4 단어)
```vue
StudyRoomAvatar.vue         # 3 단어 (최적)
ChatMessageList.vue         # 3 단어
UserProfileCard.vue         # 3 단어
FocusAnalysisChart.vue      # 3 단어
```

#### ⚠️ 주의 (너무 짧음)
```vue
Avatar.vue                  # 너무 일반적 → StudyRoomAvatar.vue
Chat.vue                    # 너무 일반적 → ChatPanel.vue
```

#### ⚠️ 주의 (너무 김)
```vue
StudyRoomVideoCallPanelControlBarButton.vue   # 너무 김!
→ VideoCallControls.vue 로 단순화
```

---

## 📝 3. 변수 및 함수 네이밍

### 3.1 변수 네이밍

#### 일반 변수
```typescript
// ✅ camelCase 사용
const userName = 'John'
const roomId = 'room-123'
const isLoggedIn = true
const focusScore = 85.5

// ❌ 나쁜 예
const UserName = 'John'       // PascalCase는 클래스/타입에만
const room_id = 'room-123'    // snake_case는 사용 안 함
const logged_in = true
```

#### Boolean 변수
```typescript
// ✅ is, has, should로 시작
const isLoading = true
const hasPermission = false
const shouldUpdate = true
const canEdit = false

// ❌ 나쁜 예
const loading = true          // is 누락
const permission = false      // has 누락
const update = true           // 동사로만 작성
```

#### 상수
```typescript
// ✅ UPPER_SNAKE_CASE
const MAX_RETRY_COUNT = 3
const API_BASE_URL = 'https://api.virtudy.com'
const DEFAULT_THEME = 'neon'

// ❌ 나쁜 예
const maxRetryCount = 3       // 상수는 대문자
const apiBaseUrl = '...'
```

#### 배열/리스트
```typescript
// ✅ 복수형 사용
const users = ['John', 'Jane']
const rooms = [room1, room2]
const messages = []

// ❌ 나쁜 예
const user = ['John', 'Jane'] // 배열은 복수형
const roomList = []           // List 접미사 불필요
const messageArray = []       // Array 접미사 불필요
```

#### Ref/Reactive 변수 (Vue 3)
```typescript
// ✅ Ref 접미사 없음 (일반 변수처럼)
const count = ref(0)
const userName = ref('')
const isVisible = ref(false)

// ❌ 나쁜 예
const countRef = ref(0)       // Ref 접미사 불필요
const userNameRef = ref('')
```

### 3.2 함수 네이밍

#### 일반 함수
```typescript
// ✅ 동사 + 명사 형태, camelCase
function fetchUserData() { }
function calculateFocusScore() { }
function formatDate() { }
function validateEmail() { }

// ❌ 나쁜 예
function user() { }           // 동사 누락
function FetchUserData() { }  // PascalCase 사용 안 함
function get_user() { }       // snake_case 사용 안 함
```

#### 이벤트 핸들러
```typescript
// ✅ handle 또는 on으로 시작
function handleClick() { }
function handleSubmit() { }
function onMessageReceived() { }
function onFocusChange() { }

// ❌ 나쁜 예
function click() { }          // handle/on 누락
function messageReceived() { }
function focusChange() { }
```

#### Getter 함수
```typescript
// ✅ get으로 시작
function getUserName() { }
function getFormattedDate() { }
function getCurrentRoom() { }

// ❌ 나쁜 예
function userName() { }       // get 누락
function fetchUserName() { }  // fetch는 비동기에 사용
```

#### Boolean 반환 함수
```typescript
// ✅ is, has, can, should로 시작
function isValid() { return true }
function hasPermission() { return false }
function canEdit() { return true }
function shouldUpdate() { return false }

// ❌ 나쁜 예
function valid() { return true }
function checkPermission() { }  // check보다 has 권장
```

#### Async 함수
```typescript
// ✅ 비동기 작업 동사 사용
async function fetchRooms() { }
async function loadUserData() { }
async function sendMessage() { }

// ❌ 나쁜 예
async function getRooms() { }    // get은 동기적 getter에 사용
async function rooms() { }       // 동사 누락
```

---

## 🏷️ 4. TypeScript 타입 네이밍

### 4.1 Interface
```typescript
// ✅ PascalCase, I 접두사 없음
interface User {
  id: string
  name: string
}

interface Room {
  id: string
  title: string
}

interface FocusData {
  score: number
  timestamp: number
}

// ❌ 나쁜 예
interface IUser { }           // I 접두사 사용 안 함
interface user { }            // camelCase 사용 안 함
interface UserInterface { }   // Interface 접미사 불필요
```

### 4.2 Type Alias
```typescript
// ✅ PascalCase
type UserId = string
type RoomStatus = 'waiting' | 'active' | 'ended'
type Callback = () => void

// ❌ 나쁜 예
type userId = string          // camelCase 사용 안 함
type ROOM_STATUS = '...'      // UPPER_CASE 사용 안 함
```

### 4.3 Enum
```typescript
// ✅ PascalCase (Enum 이름 및 값)
enum UserRole {
  Admin = 'ADMIN',
  User = 'USER',
  Guest = 'GUEST'
}

enum RoomStatus {
  Waiting = 'WAITING',
  Active = 'ACTIVE',
  Ended = 'ENDED'
}

// ❌ 나쁜 예
enum userRole { }             // camelCase 사용 안 함
enum USER_ROLE { }            // UPPER_CASE 사용 안 함
```

### 4.4 Generic 타입 파라미터
```typescript
// ✅ 단일 대문자 또는 T로 시작하는 PascalCase
function identity<T>(arg: T): T { }
function map<T, U>(items: T[], fn: (item: T) => U): U[] { }

interface Container<TValue> {
  value: TValue
}

// ❌ 나쁜 예
function identity<Type>(arg: Type) { }  // 너무 김
function map<t, u>() { }                // 소문자 사용 안 함
```

---

## 🎯 5. Composable 네이밍

### 5.1 기본 규칙
```typescript
// ✅ use로 시작, camelCase
export function useFocus() {
  const focusScore = ref(0)
  
  function analyzeFocus() { }
  
  return {
    focusScore,
    analyzeFocus
  }
}

// ❌ 나쁜 예
export function focus() { }           // use 누락
export function UseFocus() { }        // PascalCase 사용 안 함
export function use_focus() { }       // snake_case 사용 안 함
```

### 5.2 반환 객체 네이밍
```typescript
// ✅ 명확한 네이밍
export function useWebRTC() {
  return {
    localStream,       // 명사
    remoteStream,
    connect,           // 동사
    disconnect,
    isConnected        // boolean
  }
}

// ❌ 나쁜 예
export function useWebRTC() {
  return {
    stream1,           // 의미 불명확
    stream2,
    doConnect,         // do 접두사 불필요
    connected          // is 누락
  }
}
```

---

## 📦 6. Pinia Store 네이밍

### 6.1 Store 정의
```typescript
// ✅ camelCase, Store로 끝남
export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null,
    isLoggedIn: false
  }),
  
  actions: {
    async fetchUser() { },
    logout() { }
  }
})

// ❌ 나쁜 예
export const UserStore = defineStore('user', { })   // PascalCase
export const user = defineStore('user', { })        // Store 누락
export const useUser = defineStore('user', { })     // Store 누락
```

### 6.2 Store ID
```typescript
// ✅ 소문자, 하이픈 구분
defineStore('user', { })
defineStore('study-room', { })
defineStore('ranking', { })

// ❌ 나쁜 예
defineStore('User', { })          // 대문자 사용 안 함
defineStore('studyRoom', { })     // camelCase 사용 안 함
defineStore('study_room', { })    // snake_case 사용 안 함
```

---

## 🌐 7. API 엔드포인트 네이밍

### 7.1 함수 네이밍
```typescript
// ✅ HTTP 메서드 + 리소스명
export const roomAPI = {
  getRooms: () => axios.get('/rooms'),
  getRoomById: (id: string) => axios.get(`/rooms/${id}`),
  createRoom: (data: CreateRoomDto) => axios.post('/rooms', data),
  updateRoom: (id: string, data: UpdateRoomDto) => axios.put(`/rooms/${id}`, data),
  deleteRoom: (id: string) => axios.delete(`/rooms/${id}`)
}

// ❌ 나쁜 예
export const roomAPI = {
  fetchRooms: () => { },        // fetch보다 get 권장
  room: () => { },              // 동사 누락
  create: () => { }             // 리소스명 누락
}
```

### 7.2 URL 패스
```typescript
// ✅ kebab-case, 복수형
'/api/rooms'
'/api/study-sessions'
'/api/user-rankings'

// ❌ 나쁜 예
'/api/room'           // 단수형
'/api/studySessions'  // camelCase
'/api/user_rankings'  // snake_case
```

---

## 🎨 8. CSS 클래스 네이밍 (BEM)

### 8.1 기본 규칙
```css
/* ✅ kebab-case, BEM 방식 */
.study-room { }                 /* Block */
.study-room__video { }          /* Element */
.study-room__video--active { }  /* Modifier */

.chat-panel { }
.chat-panel__message { }
.chat-panel__message--unread { }

/* ❌ 나쁜 예 */
.studyRoom { }                  /* camelCase */
.study_room { }                 /* snake_case */
.StudyRoom { }                  /* PascalCase */
```

### 8.2 상태 클래스
```css
/* ✅ is-, has-로 시작 */
.is-active { }
.is-loading { }
.is-hidden { }
.has-error { }

/* ❌ 나쁜 예 */
.active { }                     /* is 누락 */
.error { }                      /* has 누락 */
```

---

## 📋 9. 네이밍 체크리스트

### ✅ 파일
- [ ] Vue 컴포넌트는 PascalCase.vue
- [ ] Composables는 useCamelCase.ts
- [ ] API 파일은 camelCaseAPI.ts
- [ ] 타입 파일은 camelCase.types.ts
- [ ] 폴더는 kebab-case/

### ✅ 코드
- [ ] 변수/함수는 camelCase
- [ ] 클래스/타입은 PascalCase
- [ ] 상수는 UPPER_SNAKE_CASE
- [ ] Boolean은 is/has/can/should로 시작
- [ ] 이벤트 핸들러는 handle/on으로 시작

### ✅ 컨벤션
- [ ] 의미 있는 이름 사용 (a, b, temp 지양)
- [ ] 줄임말 지양 (msg → message, usr → user)
- [ ] 일관성 유지 (팀 전체 동일한 규칙)

---

## 🎯 목표

> **코드를 읽는 사람이 변수/함수/파일의 역할을 즉시 이해할 수 있도록!**

- 🔍 **가독성**: 이름만 보고 역할을 파악
- 🤝 **일관성**: 팀 전체가 동일한 규칙 준수
- 🚀 **생산성**: 네이밍 고민 시간 단축