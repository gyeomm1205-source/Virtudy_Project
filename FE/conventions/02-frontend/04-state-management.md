# 상태 관리 규칙 (Pinia + Context)

> **목적**: VIRTUDY의 하이브리드 상태 관리 전략 - Pinia (저빈도) + Reactive Context (고빈도)

---

## 🎯 1. 상태 관리 전략 개요

### 1.1 문제 정의
```
❌ 기존 문제: MediaPipe의 60FPS 데이터를 Pinia에 저장
→ Pinia DevTools 오버헤드로 브라우저 멈춤
→ 불필요한 리렌더링으로 성능 저하
```

### 1.2 해결 방안: 2-Tier 아키텍처
```
┌──────────────────────────────────────────────┐
│  HIGH FREQUENCY (60 FPS)                     │
│  MediaPipe → Reactive Context → Avatar      │
│  (Pinia 우회, DevTools 미사용)              │
└──────────────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│  LOW FREQUENCY (5초 간격)                    │
│  Focus Analyzer → Pinia Store → UI/통계     │
│  (전역 상태, DevTools 활용)                  │
└──────────────────────────────────────────────┘
```

---

## 🔥 2. Reactive Context (고속 데이터)

### 2.1 사용 시기
- **60FPS 실시간 데이터** (MediaPipe 얼굴 좌표)
- **아바타 애니메이션** (실시간 렌더링)
- **즉각적인 UI 업데이트** (카메라 프리뷰)

### 2.2 Context 정의

#### core/context/HighFrequencyContext.ts
```typescript
import { InjectionKey, Ref, ref } from 'vue'

export interface HighFrequencyData {
  faceLandmarks: Ref<Float32Array | null>
  eyeGaze: Ref<{ x: number; y: number }>
  headPose: Ref<{ pitch: number; yaw: number; roll: number }>
  avatarPosition: Ref<{ x: number; y: number; z: number }>
  isFaceDetected: Ref<boolean>
}

export const HighFrequencyKey: InjectionKey<HighFrequencyData> = 
  Symbol('HighFrequencyContext')

export function createHighFrequencyContext(): HighFrequencyData {
  return {
    faceLandmarks: ref(null),
    eyeGaze: ref({ x: 0, y: 0 }),
    headPose: ref({ pitch: 0, yaw: 0, roll: 0 }),
    avatarPosition: ref({ x: 0, y: 0, z: 0 }),
    isFaceDetected: ref(false)
  }
}
```

### 2.3 Provider 구현

#### app/providers/HighFrequencyProvider.vue
```vue
<script setup lang="ts">
import { provide, onMounted, onUnmounted } from 'vue'
import { createHighFrequencyContext, HighFrequencyKey } from '@/core/context/HighFrequencyContext'
import { MediaPipeManager } from '@/core/managers/MediaPipeManager'

const context = createHighFrequencyContext()
provide(HighFrequencyKey, context)

const mediaPipe = MediaPipeManager.getInstance()

onMounted(async () => {
  await mediaPipe.initialize()
  
  // 60FPS 데이터 스트림
  mediaPipe.onResults((results) => {
    context.faceLandmarks.value = results.faceLandmarks
    context.eyeGaze.value = results.eyeGaze
    context.headPose.value = results.headPose
    context.isFaceDetected.value = results.isFaceDetected
  })
})

onUnmounted(() => {
  mediaPipe.dispose()
})
</script>

<template>
  <slot />
</template>
```

### 2.4 Consumer (사용)

#### features/study-room/ui/Avatar.vue
```vue
<script setup lang="ts">
import { inject, watchEffect } from 'vue'
import { HighFrequencyKey } from '@/core/context/HighFrequencyContext'

const context = inject(HighFrequencyKey)
if (!context) throw new Error('HighFrequencyContext not provided')

// 60FPS로 아바타 업데이트
watchEffect(() => {
  if (context.isFaceDetected.value) {
    updateAvatarPosition(context.avatarPosition.value)
    updateAvatarExpression(context.headPose.value)
  }
})

function updateAvatarPosition(pos: { x: number; y: number; z: number }) {
  // 실시간 아바타 위치 업데이트 (60FPS)
  avatarRenderer.setPosition(pos)
}
</script>

<template>
  <canvas ref="avatarCanvas" />
</template>
```

---

## 📦 3. Pinia Store (저빈도 데이터)

### 3.1 사용 시기
- **전역 상태** (사용자 정보, 인증 토큰)
- **5초 간격 집중도 통계**
- **방 정보, 랭킹 데이터**
- **영구 저장이 필요한 데이터** (LocalStorage 연동)

### 3.2 Store 구조

#### entities/user/model/userStore.ts
```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // State
  const currentUser = ref<User | null>(null)
  const isLoggedIn = ref(false)
  const accessToken = ref<string | null>(null)

  // Getters
  const userName = computed(() => currentUser.value?.name ?? 'Guest')
  const userRole = computed(() => currentUser.value?.role ?? 'guest')

  // Actions
  async function login(email: string, password: string) {
    try {
      const { data } = await authAPI.login({ email, password })
      currentUser.value = data.user
      accessToken.value = data.accessToken
      isLoggedIn.value = true
      
      // LocalStorage에 저장
      localStorage.setItem('accessToken', data.accessToken)
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }

  function logout() {
    currentUser.value = null
    accessToken.value = null
    isLoggedIn.value = false
    localStorage.removeItem('accessToken')
  }

  async function fetchProfile() {
    const { data } = await userAPI.getProfile()
    currentUser.value = data
  }

  return {
    // State
    currentUser,
    isLoggedIn,
    accessToken,
    
    // Getters
    userName,
    userRole,
    
    // Actions
    login,
    logout,
    fetchProfile
  }
})
```

#### entities/room/model/roomStore.ts
```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useRoomStore = defineStore('room', () => {
  const currentRoom = ref<Room | null>(null)
  const rooms = ref<Room[]>([])
  const participants = ref<User[]>([])

  async function fetchRooms() {
    const { data } = await roomAPI.getRooms()
    rooms.value = data
  }

  async function joinRoom(roomId: string) {
    const { data } = await roomAPI.joinRoom(roomId)
    currentRoom.value = data.room
    participants.value = data.participants
  }

  async function leaveRoom() {
    if (!currentRoom.value) return
    await roomAPI.leaveRoom(currentRoom.value.id)
    currentRoom.value = null
    participants.value = []
  }

  return {
    currentRoom,
    rooms,
    participants,
    fetchRooms,
    joinRoom,
    leaveRoom
  }
})
```

#### entities/focus/model/focusStore.ts
```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useFocusStore = defineStore('focus', () => {
  // ✅ 5초 간격 집중도 통계 (Pinia에 저장)
  const focusHistory = ref<FocusRecord[]>([])
  const averageFocusScore = ref(0)
  const totalStudyTime = ref(0)

  function addFocusRecord(record: FocusRecord) {
    focusHistory.value.push(record)
    
    // 평균 계산
    const sum = focusHistory.value.reduce((acc, r) => acc + r.score, 0)
    averageFocusScore.value = sum / focusHistory.value.length
  }

  function resetFocus() {
    focusHistory.value = []
    averageFocusScore.value = 0
    totalStudyTime.value = 0
  }

  return {
    focusHistory,
    averageFocusScore,
    totalStudyTime,
    addFocusRecord,
    resetFocus
  }
})
```

---

## 🔄 4. Context와 Pinia 통합

### 4.1 데이터 흐름

```
MediaPipe (60 FPS)
    ↓
Reactive Context (고속 바이패스)
    ↓
Avatar 컴포넌트 (60 FPS 렌더링)

────────────────────────────────

Focus Analyzer (5초 간격)
    ↓
Pinia Store (focusStore)
    ↓
통계 UI 컴포넌트
```

### 4.2 통합 예시

#### features/study-room/logic/useFocus.ts
```typescript
import { inject } from 'vue'
import { useFocusStore } from '@/entities/focus/model/focusStore'
import { HighFrequencyKey } from '@/core/context/HighFrequencyContext'

export function useFocus() {
  const context = inject(HighFrequencyKey)
  const focusStore = useFocusStore()

  // 5초마다 집중도 분석 및 Pinia 저장
  setInterval(() => {
    if (context?.isFaceDetected.value) {
      const score = analyzeFocusScore(context.headPose.value)
      
      // Pinia에 저장 (5초 간격)
      focusStore.addFocusRecord({
        score,
        timestamp: Date.now()
      })
    }
  }, 5000)

  function analyzeFocusScore(headPose: HeadPose): number {
    // 집중도 계산 로직...
    return score
  }

  return {
    analyzeFocusScore
  }
}
```

---

## 📋 5. 상태 관리 규칙

### 5.1 Context 사용 규칙

#### ✅ Context에 저장해야 하는 것
- MediaPipe 얼굴 좌표 (60 FPS)
- 실시간 아바타 데이터
- 카메라 프리뷰 프레임
- 마우스/터치 위치 (고빈도 입력)

#### ❌ Context에 저장하면 안 되는 것
- 사용자 정보 (전역 상태 → Pinia)
- API 응답 데이터 (전역 상태 → Pinia)
- 로그인 토큰 (영구 저장 → Pinia + LocalStorage)
- 방 목록, 랭킹 데이터 (전역 상태 → Pinia)

### 5.2 Pinia 사용 규칙

#### ✅ Pinia에 저장해야 하는 것
- 사용자 인증 정보
- 방 정보, 참여자 목록
- 5초 간격 집중도 통계
- 랭킹, 리포트 데이터
- LocalStorage 연동 데이터

#### ❌ Pinia에 저장하면 안 되는 것
- 60 FPS MediaPipe 데이터 (성능 문제 → Context)
- 실시간 아바타 좌표 (성능 문제 → Context)
- 일회성 UI 상태 (컴포넌트 local state 사용)

---

## 🎯 6. 실전 예시

### 시나리오 1: 새 엔티티 추가 (Pinia)

#### Step 1: Store 정의
```typescript
// entities/ranking/model/rankingStore.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useRankingStore = defineStore('ranking', () => {
  const rankings = ref<Ranking[]>([])
  const myRank = ref<number | null>(null)

  async function fetchRankings() {
    const { data } = await rankingAPI.getRankings()
    rankings.value = data
  }

  return {
    rankings,
    myRank,
    fetchRankings
  }
})
```

#### Step 2: 컴포넌트에서 사용
```vue
<script setup lang="ts">
import { useRankingStore } from '@/entities/ranking/model/rankingStore'
import { onMounted } from 'vue'

const rankingStore = useRankingStore()

onMounted(async () => {
  await rankingStore.fetchRankings()
})
</script>

<template>
  <ul>
    <li v-for="rank in rankingStore.rankings" :key="rank.id">
      {{ rank.userName }}: {{ rank.score }}점
    </li>
  </ul>
</template>
```

### 시나리오 2: 고속 데이터 추가 (Context)

#### Step 1: Context 확장
```typescript
// core/context/HighFrequencyContext.ts
export interface HighFrequencyData {
  faceLandmarks: Ref<Float32Array | null>
  // 🆕 새로운 고속 데이터 추가
  handGesture: Ref<HandGesture | null>
}

export function createHighFrequencyContext(): HighFrequencyData {
  return {
    faceLandmarks: ref(null),
    handGesture: ref(null)
  }
}
```

#### Step 2: Provider 업데이트
```vue
<script setup lang="ts">
// app/providers/HighFrequencyProvider.vue
mediaPipe.onResults((results) => {
  context.faceLandmarks.value = results.faceLandmarks
  context.handGesture.value = results.handGesture  // 🆕
})
</script>
```

#### Step 3: Consumer에서 사용
```vue
<script setup lang="ts">
const context = inject(HighFrequencyKey)

watchEffect(() => {
  if (context?.handGesture.value) {
    handleGesture(context.handGesture.value)
  }
})
</script>
```

---

## 🚀 7. 성능 최적화 팁

### 7.1 Pinia 최적화

#### 필요한 상태만 구독
```typescript
// ❌ 나쁜 예: 전체 store 구독
const userStore = useUserStore()
watch(() => userStore.$state, () => {
  console.log('Store changed')
}, { deep: true })  // 모든 변경 감지 → 성능 저하

// ✅ 좋은 예: 필요한 state만 구독
const { userName } = storeToRefs(userStore)
watch(userName, (newName) => {
  console.log('User name changed:', newName)
})
```

#### Computed 활용
```typescript
// ✅ Computed로 파생 상태 캐싱
const userName = computed(() => currentUser.value?.name ?? 'Guest')
const isAdmin = computed(() => currentUser.value?.role === 'admin')
```

### 7.2 Context 최적화

#### watchEffect 대신 watch 사용 (필요시)
```typescript
// ⚠️ watchEffect: 모든 반응형 데이터 추적
watchEffect(() => {
  if (context.isFaceDetected.value) {
    updateAvatar(context.avatarPosition.value)
  }
})

// ✅ watch: 특정 데이터만 추적
watch(() => context.avatarPosition.value, (pos) => {
  updateAvatar(pos)
})
```

---

## 📌 8. 체크리스트

### ✅ Context 사용 시
- [ ] 데이터 업데이트 빈도가 30FPS 이상인가?
- [ ] DevTools 없이 직접 렌더링해야 하는가?
- [ ] 전역 상태로 관리할 필요가 없는가?

### ✅ Pinia 사용 시
- [ ] 전역에서 접근해야 하는 데이터인가?
- [ ] DevTools로 디버깅이 필요한가?
- [ ] LocalStorage와 연동이 필요한가?
- [ ] 데이터 업데이트 빈도가 5초 이상인가?

---

## 🎯 목표

> **성능과 개발 경험(DX) 모두 만족시키는 하이브리드 상태 관리!**

- ⚡ **성능**: 60 FPS 아바타 렌더링 (Context)
- 🛠️ **DX**: DevTools로 편리한 디버깅 (Pinia)
- 🔄 **유지보수**: 명확한 역할 분리
- 📊 **확장성**: 새로운 데이터 타입 추가 용이