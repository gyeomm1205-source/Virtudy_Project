# 테스트 전략

> **목적**: VIRTUDY 프로젝트의 품질 보장을 위한 체계적인 테스트 전략

---

## 🎯 1. 테스트 피라미드

```
        ┌─────────────┐
        │   E2E (5%)  │  전체 시나리오 테스트
        ├─────────────┤
        │ Integration │  컴포넌트 통합 테스트
        │    (15%)    │
        ├─────────────┤
        │   Unit      │  단위 함수 테스트
        │   (80%)     │
        └─────────────┘
```

### 1.1 테스트 비율 가이드
- **Unit Test (80%)**: 유틸리티, Composables, Store
- **Integration Test (15%)**: 컴포넌트 상호작용
- **E2E Test (5%)**: 핵심 사용자 시나리오

---

## 🧪 2. Unit Test (단위 테스트)

### 2.1 도구
- **Vitest**: 빠른 단위 테스트 러너
- **@vue/test-utils**: Vue 컴포넌트 테스트 유틸리티

### 2.2 유틸리티 함수 테스트

#### 예시: format.ts
```typescript
// shared/lib/format.ts
export function formatDate(date: Date): string {
  return date.toLocaleDateString('ko-KR')
}

export function formatDuration(seconds: number): string {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}
```

#### 테스트: format.test.ts
```typescript
import { describe, it, expect } from 'vitest'
import { formatDate, formatDuration } from './format'

describe('format', () => {
  describe('formatDate', () => {
    it('should format date correctly', () => {
      const date = new Date('2026-01-19')
      expect(formatDate(date)).toBe('2026. 1. 19.')
    })
  })

  describe('formatDuration', () => {
    it('should format seconds to HH:MM:SS', () => {
      expect(formatDuration(3661)).toBe('1:01:01')
      expect(formatDuration(90)).toBe('0:01:30')
      expect(formatDuration(0)).toBe('0:00:00')
    })

    it('should pad minutes and seconds with zero', () => {
      expect(formatDuration(5)).toBe('0:00:05')
      expect(formatDuration(65)).toBe('0:01:05')
    })
  })
})
```

### 2.3 Composables 테스트

#### 예시: useTimer.ts
```typescript
// features/study-room/logic/useTimer.ts
import { ref } from 'vue'

export function useTimer() {
  const seconds = ref(0)
  const isRunning = ref(false)
  let intervalId: number | null = null

  function start() {
    if (isRunning.value) return
    isRunning.value = true
    intervalId = setInterval(() => {
      seconds.value++
    }, 1000)
  }

  function pause() {
    if (!isRunning.value) return
    isRunning.value = false
    if (intervalId) clearInterval(intervalId)
  }

  function reset() {
    pause()
    seconds.value = 0
  }

  return {
    seconds,
    isRunning,
    start,
    pause,
    reset
  }
}
```

#### 테스트: useTimer.test.ts
```typescript
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useTimer } from './useTimer'

describe('useTimer', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should start timer', () => {
    const { seconds, isRunning, start } = useTimer()
    
    expect(seconds.value).toBe(0)
    expect(isRunning.value).toBe(false)
    
    start()
    expect(isRunning.value).toBe(true)
    
    vi.advanceTimersByTime(3000)
    expect(seconds.value).toBe(3)
  })

  it('should pause timer', () => {
    const { seconds, isRunning, start, pause } = useTimer()
    
    start()
    vi.advanceTimersByTime(2000)
    expect(seconds.value).toBe(2)
    
    pause()
    expect(isRunning.value).toBe(false)
    
    vi.advanceTimersByTime(2000)
    expect(seconds.value).toBe(2)  // 멈췄으므로 그대로
  })

  it('should reset timer', () => {
    const { seconds, isRunning, start, reset } = useTimer()
    
    start()
    vi.advanceTimersByTime(5000)
    expect(seconds.value).toBe(5)
    
    reset()
    expect(seconds.value).toBe(0)
    expect(isRunning.value).toBe(false)
  })
})
```

### 2.4 Pinia Store 테스트

#### 테스트: userStore.test.ts
```typescript
import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useUserStore } from './userStore'
import * as authAPI from '../api/authAPI'

vi.mock('../api/authAPI')

describe('userStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should login successfully', async () => {
    const store = useUserStore()
    
    const mockUser = { id: '1', name: 'John', role: 'user' }
    vi.mocked(authAPI.login).mockResolvedValue({
      data: {
        user: mockUser,
        accessToken: 'token123'
      }
    })
    
    await store.login('test@example.com', 'password')
    
    expect(store.currentUser).toEqual(mockUser)
    expect(store.isLoggedIn).toBe(true)
    expect(store.accessToken).toBe('token123')
  })

  it('should logout', () => {
    const store = useUserStore()
    
    store.currentUser = { id: '1', name: 'John', role: 'user' }
    store.isLoggedIn = true
    store.accessToken = 'token123'
    
    store.logout()
    
    expect(store.currentUser).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(store.accessToken).toBeNull()
  })
})
```

---

## 🧩 3. Integration Test (통합 테스트)

### 3.1 컴포넌트 테스트

#### 예시: Button.vue
```vue
<!-- shared/ui/Button.vue -->
<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary'
  disabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'click'): void
}>()
</script>

<template>
  <button
    :class="['btn', `btn--${variant}`]"
    :disabled="disabled"
    @click="emit('click')"
  >
    <slot />
  </button>
</template>
```

#### 테스트: Button.test.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Button from './Button.vue'

describe('Button', () => {
  it('should render slot content', () => {
    const wrapper = mount(Button, {
      slots: {
        default: 'Click me'
      }
    })
    
    expect(wrapper.text()).toBe('Click me')
  })

  it('should emit click event', async () => {
    const wrapper = mount(Button)
    
    await wrapper.trigger('click')
    
    expect(wrapper.emitted('click')).toHaveLength(1)
  })

  it('should not emit click when disabled', async () => {
    const wrapper = mount(Button, {
      props: {
        disabled: true
      }
    })
    
    await wrapper.trigger('click')
    
    expect(wrapper.emitted('click')).toBeUndefined()
  })

  it('should apply variant class', () => {
    const wrapper = mount(Button, {
      props: {
        variant: 'secondary'
      }
    })
    
    expect(wrapper.classes()).toContain('btn--secondary')
  })
})
```

### 3.2 Composable과 Component 통합

#### 테스트: TimerPanel.test.ts
```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import TimerPanel from './TimerPanel.vue'

describe('TimerPanel', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  it('should display initial time as 00:00:00', () => {
    const wrapper = mount(TimerPanel)
    expect(wrapper.find('.timer-display').text()).toBe('00:00:00')
  })

  it('should start timer on button click', async () => {
    const wrapper = mount(TimerPanel)
    
    await wrapper.find('.btn-start').trigger('click')
    
    vi.advanceTimersByTime(3000)
    await wrapper.vm.$nextTick()
    
    expect(wrapper.find('.timer-display').text()).toBe('00:00:03')
  })

  it('should pause timer', async () => {
    const wrapper = mount(TimerPanel)
    
    await wrapper.find('.btn-start').trigger('click')
    vi.advanceTimersByTime(2000)
    
    await wrapper.find('.btn-pause').trigger('click')
    vi.advanceTimersByTime(2000)
    await wrapper.vm.$nextTick()
    
    expect(wrapper.find('.timer-display').text()).toBe('00:00:02')
  })
})
```

---

## 🌐 4. E2E Test (End-to-End 테스트)

### 4.1 도구
- **Playwright**: 현대적인 E2E 테스트 프레임워크
- **Cypress**: 대안 (선택 가능)

### 4.2 핵심 시나리오 테스트

#### 시나리오 1: 로그인 → 스터디룸 입장
```typescript
// tests/e2e/study-flow.spec.ts
import { test, expect } from '@playwright/test'

test.describe('Study Flow', () => {
  test('should login and join study room', async ({ page }) => {
    // 1. 로그인 페이지 이동
    await page.goto('/login')
    
    // 2. 로그인 폼 입력
    await page.fill('input[name="email"]', 'test@example.com')
    await page.fill('input[name="password"]', 'password123')
    await page.click('button[type="submit"]')
    
    // 3. 홈으로 리다이렉트 확인
    await expect(page).toHaveURL('/home')
    
    // 4. 스터디룸 목록 확인
    await expect(page.locator('.room-card')).toHaveCount(3)
    
    // 5. 첫 번째 방 입장
    await page.click('.room-card:first-child .btn-join')
    
    // 6. 스터디룸 페이지 확인
    await expect(page).toHaveURL(/\/study-room\//)
    await expect(page.locator('.avatar-canvas')).toBeVisible()
    await expect(page.locator('.timer-panel')).toBeVisible()
  })

  test('should start timer and track focus', async ({ page }) => {
    // 로그인 및 방 입장 (beforeEach에서 처리 가능)
    await page.goto('/study-room/test-room-id')
    
    // 타이머 시작
    await page.click('.btn-start-timer')
    await expect(page.locator('.timer-display')).toContainText('00:00:')
    
    // 5초 대기 후 집중도 차트 확인
    await page.waitForTimeout(6000)
    await expect(page.locator('.focus-chart canvas')).toBeVisible()
  })
})
```

#### 시나리오 2: 회원가입 플로우
```typescript
// tests/e2e/signup-flow.spec.ts
import { test, expect } from '@playwright/test'

test.describe('Signup Flow', () => {
  test('should complete signup process', async ({ page }) => {
    // 1. 회원가입 페이지 이동
    await page.goto('/signup')
    
    // 2. 폼 입력
    await page.fill('input[name="email"]', 'newuser@example.com')
    await page.fill('input[name="password"]', 'SecurePass123!')
    await page.fill('input[name="passwordConfirm"]', 'SecurePass123!')
    await page.fill('input[name="name"]', 'New User')
    
    // 3. 약관 동의
    await page.check('input[name="termsAgree"]')
    
    // 4. 회원가입 버튼 클릭
    await page.click('button[type="submit"]')
    
    // 5. 설문 페이지로 이동 확인
    await expect(page).toHaveURL('/survey')
    
    // 6. 설문 완료
    await page.click('input[value="coding"]')
    await page.click('button.btn-submit')
    
    // 7. 홈으로 이동 확인
    await expect(page).toHaveURL('/home')
  })
})
```

---

## 🚀 5. 테스트 실행

### 5.1 package.json 스크립트
```json
{
  "scripts": {
    "test": "vitest",
    "test:unit": "vitest run",
    "test:watch": "vitest watch",
    "test:coverage": "vitest run --coverage",
    "test:e2e": "playwright test",
    "test:e2e:ui": "playwright test --ui"
  }
}
```

### 5.2 실행 명령어
```bash
# Unit/Integration 테스트
npm run test              # Watch 모드로 실행
npm run test:unit         # 한 번 실행 후 종료
npm run test:coverage     # 커버리지 리포트 생성

# E2E 테스트
npm run test:e2e          # Headless 모드
npm run test:e2e:ui       # UI 모드 (디버깅 편함)
```

---

## 📊 6. 테스트 커버리지 목표

### 6.1 커버리지 기준
```
- Line Coverage: 80% 이상
- Branch Coverage: 70% 이상
- Function Coverage: 80% 이상
```

### 6.2 vitest.config.ts
```typescript
import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      lines: 80,
      branches: 70,
      functions: 80,
      statements: 80
    }
  }
})
```

---

## 🎯 7. 테스트 우선순위

### 7.1 높은 우선순위 (필수)
1. **인증 관련**: 로그인, 회원가입, 로그아웃
2. **핵심 비즈니스 로직**: 집중도 분석, 타이머
3. **데이터 변환**: 유틸리티 함수, 포맷터
4. **API 통신**: Store actions

### 7.2 중간 우선순위
1. **UI 컴포넌트**: Button, Modal, Input
2. **Composables**: useWebRTC, useSocket
3. **라우터 가드**: 인증 체크

### 7.3 낮은 우선순위
1. **스타일 테스트**: CSS 클래스 확인
2. **정적 컴포넌트**: 레이아웃, 텍스트 표시

---

## 📋 8. 테스트 작성 체크리스트

### ✅ Unit Test
- [ ] 함수의 입력/출력이 명확한가?
- [ ] 엣지 케이스를 테스트했는가? (null, undefined, 빈 배열)
- [ ] 에러 케이스를 테스트했는가?
- [ ] Mock을 적절히 사용했는가?

### ✅ Integration Test
- [ ] 컴포넌트가 올바르게 렌더링되는가?
- [ ] 이벤트가 정상적으로 발생하는가?
- [ ] Props/Emits가 올바르게 동작하는가?
- [ ] 상태 변화가 UI에 반영되는가?

### ✅ E2E Test
- [ ] 핵심 사용자 시나리오를 커버하는가?
- [ ] 실제 사용자처럼 테스트하는가?
- [ ] 네트워크 요청을 기다리는가? (waitForResponse)
- [ ] 테스트 데이터를 정리하는가? (cleanup)

---

## 🎯 목표

> **자동화된 테스트로 안정적인 코드베이스 유지!**

- 🛡️ **안정성**: 버그를 배포 전에 발견
- 🚀 **자신감**: 리팩토링 시 회귀 버그 방지
- 📚 **문서화**: 테스트 코드가 사용 예시
- ⚡ **생산성**: 수동 테스트 시간 단축