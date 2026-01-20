# 코딩 스타일 가이드 (ESLint + Prettier)

> **목적**: 일관되고 읽기 쉬운 코드 작성을 위한 스타일 가이드 및 자동화 도구 설정

---

## 🎨 1. 코드 포맷팅 도구

### 1.1 ESLint (문법 검사)
- **역할**: 코드 품질 및 잠재적 버그 검사
- **설정 파일**: `.eslintrc.cjs`

### 1.2 Prettier (코드 포맷터)
- **역할**: 코드 스타일 자동 정리
- **설정 파일**: `.prettierrc`

### 1.3 도구 통합 원칙
```
Prettier: 코드 포맷팅 (들여쓰기, 줄바꿈, 따옴표 등)
ESLint: 코드 품질 (변수 미사용, 타입 오류 등)
```

---

## ⚙️ 2. ESLint 설정

### 2.1 .eslintrc.cjs
```javascript
module.exports = {
  root: true,
  env: {
    browser: true,
    es2021: true,
    node: true
  },
  extends: [
    'plugin:vue/vue3-recommended',      // Vue 3 권장 규칙
    'eslint:recommended',                // ESLint 기본 규칙
    '@vue/eslint-config-typescript',     // TypeScript 규칙
    '@vue/eslint-config-prettier'        // Prettier와 충돌 방지
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module'
  },
  rules: {
    // Vue 규칙
    'vue/multi-word-component-names': 'off',  // 단일 단어 컴포넌트 허용
    'vue/no-v-html': 'warn',                   // v-html 경고 (XSS 주의)
    'vue/require-default-prop': 'error',       // prop default 필수
    'vue/require-prop-types': 'error',         // prop type 필수
    'vue/component-name-in-template-casing': ['error', 'PascalCase'],  // 컴포넌트 PascalCase
    
    // TypeScript 규칙
    '@typescript-eslint/no-unused-vars': ['error', {
      argsIgnorePattern: '^_',   // _로 시작하는 변수는 미사용 허용
      varsIgnorePattern: '^_'
    }],
    '@typescript-eslint/no-explicit-any': 'warn',  // any 타입 경고
    
    // 일반 규칙
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',  // 프로덕션에서 console 경고
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'prefer-const': 'error',      // const 사용 강제
    'no-var': 'error',             // var 사용 금지
    'eqeqeq': ['error', 'always'], // === 사용 강제
  }
}
```

### 2.2 주요 규칙 설명

#### Vue 컴포넌트 규칙
```vue
<!-- ✅ 좋은 예 -->
<template>
  <StudyRoom />  <!-- PascalCase -->
</template>

<script setup lang="ts">
defineProps<{
  title: string  // prop type 명시
}>()
</script>

<!-- ❌ 나쁜 예 -->
<template>
  <study-room />  <!-- kebab-case 금지 -->
</template>

<script setup lang="ts">
defineProps({
  title: String  // TypeScript에서는 타입 명시 권장
})
</script>
```

#### TypeScript 규칙
```typescript
// ✅ 좋은 예
const userName: string = 'John'
const _unusedVar = 'ok'  // _ 접두사로 미사용 허용

function fetchUser(id: string): Promise<User> {
  return axios.get(`/users/${id}`)
}

// ❌ 나쁜 예
let userName: any = 'John'  // any 타입 경고
const unusedVar = 'error'   // 미사용 변수 에러

function fetchUser(id) {    // 타입 명시 없음
  return axios.get(`/users/${id}`)
}
```

#### 일반 코드 규칙
```typescript
// ✅ 좋은 예
const API_URL = 'https://api.virtudy.com'  // const 사용
if (userName === 'John') { }               // === 사용

// ❌ 나쁜 예
var API_URL = 'https://api.virtudy.com'    // var 금지
if (userName == 'John') { }                // == 금지
```

---

## 🎨 3. Prettier 설정

### 3.1 .prettierrc
```json
{
  "semi": false,                    // 세미콜론 생략
  "singleQuote": true,              // 작은따옴표 사용
  "tabWidth": 2,                    // 들여쓰기 2칸
  "trailingComma": "es5",           // ES5 호환 trailing comma
  "printWidth": 100,                // 최대 줄 길이 100자
  "arrowParens": "always",          // 화살표 함수 괄호 항상 사용
  "endOfLine": "lf",                // LF 줄바꿈 (Unix 스타일)
  "vueIndentScriptAndStyle": false  // Vue script/style 들여쓰기 안 함
}
```

### 3.2 주요 규칙 설명

#### 세미콜론
```typescript
// ✅ Prettier 적용 (semi: false)
const userName = 'John'
const roomId = 'room-123'

function hello() {
  console.log('Hello')
}

// ❌ 세미콜론 있으면 자동 제거됨
const userName = 'John';  // → const userName = 'John'
```

#### 따옴표
```typescript
// ✅ Prettier 적용 (singleQuote: true)
const message = 'Hello, World!'
import { useRouter } from 'vue-router'

// ❌ 큰따옴표는 자동 변환됨
const message = "Hello"  // → const message = 'Hello'
```

#### 들여쓰기
```typescript
// ✅ Prettier 적용 (tabWidth: 2)
function example() {
  if (condition) {
    doSomething()
  }
}

// ❌ 4칸 들여쓰기는 자동 수정됨
function example() {
    if (condition) {  // → 2칸으로 변경
        doSomething()
    }
}
```

#### 줄 길이
```typescript
// ✅ Prettier 적용 (printWidth: 100)
const longText = 'This is a very long text that exceeds the maximum line width and will be automatically wrapped by Prettier'

// → 자동 줄바꿈
const longText =
  'This is a very long text that exceeds the maximum line width ' +
  'and will be automatically wrapped by Prettier'
```

#### Trailing Comma
```typescript
// ✅ Prettier 적용 (trailingComma: "es5")
const user = {
  name: 'John',
  age: 30,      // trailing comma (ES5 호환)
}

const arr = [
  'apple',
  'banana',     // trailing comma
]

// ❌ 함수 파라미터는 trailing comma 없음 (ES5 비호환)
function example(a, b, c) { }  // c 뒤에 comma 없음
```

---

## 🚀 4. VSCode 설정

### 4.1 .vscode/settings.json
```json
{
  // 저장 시 자동 포맷팅
  "editor.formatOnSave": true,
  
  // Prettier를 기본 포맷터로 설정
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  
  // 파일 타입별 포맷터 설정
  "[vue]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },
  "[typescript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },
  "[javascript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },
  
  // ESLint 설정
  "eslint.validate": [
    "javascript",
    "typescript",
    "vue"
  ],
  
  // 저장 시 ESLint 자동 수정
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  
  // Volar (Vue 3) 설정
  "vue.inlayHints.inlineHandlerLeading": true,
  "vue.inlayHints.missingProps": true
}
```

### 4.2 필수 VSCode 확장 프로그램
```
1. ESLint (dbaeumer.vscode-eslint)
2. Prettier (esbenp.prettier-vscode)
3. Vue - Official (Vue.volar)
4. TypeScript Vue Plugin (Vue.vscode-typescript-vue-plugin)
```

---

## 🎯 5. 코딩 스타일 규칙

### 5.1 Vue 컴포넌트 구조

#### ✅ 권장 순서
```vue
<script setup lang="ts">
// 1. Import 문
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Button from '@/shared/ui/Button.vue'

// 2. Props 정의
const props = defineProps<{
  title: string
  count?: number
}>()

// 3. Emits 정의
const emit = defineEmits<{
  (e: 'update', value: number): void
}>()

// 4. Composables
const router = useRouter()
const { focusScore } = useFocus()

// 5. Reactive 변수
const count = ref(0)
const doubleCount = computed(() => count.value * 2)

// 6. 함수
function handleClick() {
  count.value++
}

// 7. 생명주기 훅
onMounted(() => {
  console.log('Mounted')
})
</script>

<template>
  <div class="container">
    <h1>{{ title }}</h1>
    <Button @click="handleClick">
      Count: {{ count }}
    </Button>
  </div>
</template>

<style scoped>
.container {
  padding: 20px;
}
</style>
```

### 5.2 TypeScript 타입 정의

#### Interface vs Type
```typescript
// ✅ Interface 사용 (확장 가능할 때)
interface User {
  id: string
  name: string
}

interface Admin extends User {
  role: string
}

// ✅ Type 사용 (Union, Intersection)
type Status = 'active' | 'inactive'
type UserWithStatus = User & { status: Status }
```

#### 타입 명시
```typescript
// ✅ 명시적 타입 지정 (함수 반환 타입)
function fetchUser(id: string): Promise<User> {
  return axios.get(`/users/${id}`)
}

// ✅ 명시적 타입 지정 (변수)
const userName: string = 'John'
const users: User[] = []

// ⚠️ 타입 추론 가능하면 생략 가능
const count = 0  // number로 추론됨
const message = 'Hello'  // string으로 추론됨
```

### 5.3 함수 작성 스타일

#### 화살표 함수 vs 일반 함수
```typescript
// ✅ 화살표 함수 (권장 - 짧은 로직)
const double = (x: number) => x * 2
const greet = (name: string) => `Hello, ${name}!`

// ✅ 일반 함수 (복잡한 로직)
function calculateFocusScore(data: FocusData): number {
  // 복잡한 로직...
  return score
}
```

#### 함수 파라미터
```typescript
// ✅ 많은 파라미터는 객체로
interface CreateRoomParams {
  title: string
  maxUsers: number
  isPrivate: boolean
}

function createRoom(params: CreateRoomParams) {
  // ...
}

// ❌ 나쁜 예 (파라미터 4개 이상)
function createRoom(title: string, maxUsers: number, isPrivate: boolean, password: string) {
  // ...
}
```

### 5.4 Import 순서
```typescript
// ✅ 권장 순서
// 1. Vue 관련
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 2. 외부 라이브러리
import axios from 'axios'
import { Chart } from 'chart.js'

// 3. 프로젝트 내부 (절대 경로)
import Button from '@/shared/ui/Button.vue'
import { useAuth } from '@/features/auth'

// 4. 상대 경로
import { useFocus } from '../logic/useFocus'
import Avatar from './Avatar.vue'
```

### 5.5 주석 작성

#### JSDoc 주석
```typescript
/**
 * 집중도 점수를 계산합니다.
 * @param data - 집중도 원시 데이터
 * @returns 0-100 사이의 집중도 점수
 */
function calculateFocusScore(data: FocusData): number {
  // ...
}
```

#### 일반 주석
```typescript
// ✅ 좋은 주석 (왜 이렇게 했는지 설명)
// MediaPipe는 60FPS 데이터를 생성하므로 Pinia 대신 Context 사용
const context = useHighFrequencyContext()

// ❌ 나쁜 주석 (코드가 이미 설명함)
// userName을 'John'으로 설정
const userName = 'John'
```

---

## 🔧 6. Git Hooks (Husky + lint-staged)

### 6.1 패키지 설치
```bash
npm install -D husky lint-staged
npx husky install
npx husky add .husky/pre-commit "npx lint-staged"
```

### 6.2 package.json 설정
```json
{
  "lint-staged": {
    "*.{vue,ts,js}": [
      "eslint --fix",
      "prettier --write"
    ]
  }
}
```

### 6.3 동작 방식
```
git commit 실행 시:
  1. lint-staged 실행
  2. 변경된 .vue/.ts/.js 파일에 대해:
     - ESLint로 문법 검사 및 자동 수정
     - Prettier로 코드 포맷팅
  3. 문제 없으면 커밋 완료
  4. 문제 있으면 커밋 거부
```

---

## 📋 7. 코딩 스타일 체크리스트

### ✅ 코드 작성 전
- [ ] ESLint, Prettier VSCode 확장 설치
- [ ] `.eslintrc.cjs`, `.prettierrc` 파일 확인
- [ ] VSCode 설정에서 "저장 시 포맷팅" 활성화

### ✅ 코드 작성 중
- [ ] 저장할 때마다 자동 포맷팅 확인
- [ ] ESLint 경고/에러 즉시 수정
- [ ] 타입 명시 (any 사용 지양)
- [ ] 의미 있는 변수/함수명 사용

### ✅ 커밋 전
- [ ] `npm run lint` 실행 (에러 없는지 확인)
- [ ] `npm run format` 실행 (포맷팅 확인)
- [ ] Husky pre-commit hook 통과

---

## 🎯 목표

> **코드 스타일 통일로 협업 효율 극대화!**

- 🤖 **자동화**: 저장 시 자동 포맷팅, 커밋 시 자동 검사
- 🎨 **일관성**: 팀 전체가 동일한 스타일
- 🚀 **생산성**: 스타일 논쟁 시간 0, 코딩에 집중
- ✨ **품질**: ESLint로 잠재적 버그 사전 방지