# VIRTUDY 프로젝트 컨벤션 (Conventions)

## 📘 개요

이 디렉토리는 **VIRTUDY 프로젝트의 모든 개발 규칙과 컨벤션**을 정의합니다.  
팀원 모두가 일관된 방식으로 코드를 작성하고, Git을 관리하며, 테스트를 수행할 수 있도록 명확한 가이드라인을 제공합니다.

---

## 🎯 목적

1. **일관성**: 모든 팀원이 동일한 규칙을 따라 코드 리뷰 비용을 줄입니다.
2. **자동화**: 컨벤션을 도구(ESLint, Prettier, Commitlint)로 강제하여 실수를 방지합니다.
3. **협업 효율**: 명확한 규칙으로 Git 충돌을 최소화하고 PR 프로세스를 간소화합니다.
4. **품질 보증**: 테스트 전략과 코딩 스타일 규칙으로 코드 품질을 유지합니다.

---

## 📂 전체 목차

### 📁 01-git/ - Git 관리 규칙
| 문서 | 설명 |
|------|------|
| [01-branch-strategy.md](./01-git/01-branch-strategy.md) | 브랜치 전략 (main, dev, feature, release, hotfix) |
| [02-commit-message.md](./01-git/02-commit-message.md) | 커밋 메시지 규칙 (Conventional Commits) |
| [03-pull-request-rules.md](./01-git/03-pull-request-rules.md) | PR 작성 규칙 및 AI 매뉴얼 |

### 📁 02-frontend/ - 프론트엔드 개발 규칙
| 문서 | 설명 |
|------|------|
| [01-directory-structure.md](./02-frontend/01-directory-structure.md) | 디렉토리 구조 (Feature-Sliced Design) |
| [02-naming.md](./02-frontend/02-naming.md) | 네이밍 규칙 (파일, 변수, 함수, 컴포넌트) |
| [03-coding-style.md](./02-frontend/03-coding-style.md) | 코딩 스타일 (ESLint, Prettier) |
| [04-state-management.md](./02-frontend/04-state-management.md) | 상태 관리 (Pinia, Reactive Context) |
| [05-testing.md](./02-frontend/05-testing.md) | 테스트 전략 (Unit, Integration, E2E) |

---

## 🚀 빠른 시작

### 1. Git 관련 규칙
```bash
# 브랜치 생성
git checkout -b feat/PRJ-123-login-feature

# 커밋
git commit -m "feat(auth): Add Google social login feature"

# PR 생성
# 📝 conventions/01-git/03-pull-request-rules.md 참조
```

### 2. 프론트엔드 개발 규칙
```typescript
// 파일명: PascalCase.tsx
// 컴포넌트명: PascalCase
// 변수/함수: camelCase
// 상수: UPPER_SNAKE_CASE

// ✅ Good
const userName = 'John';
const MAX_RETRIES = 3;
function fetchUserData() {}
```

### 3. 상태 관리
```typescript
// 서버 상태 → Pinia Store (Low Frequency)
// 고속 데이터 → Reactive Context (High Frequency)

// ✅ Pinia Store (5초마다 업데이트)
export const useUserStore = defineStore('user', {
  state: () => ({
    focusScore: 0,  // 요약 데이터만
  })
})

// ✅ Reactive Context (60 FPS)
const highFreqData = inject(HighFrequencyKey)
// 얼굴 좌표, 아바타 위치 등
```

---

## 📊 컨벤션 적용 범위

| 항목 | 도구 | 자동화 여부 |
|------|------|------------|
| **브랜치명** | Git Hooks | ✅ 자동 검증 |
| **커밋 메시지** | Commitlint | ✅ 자동 검증 |
| **코드 스타일** | Prettier | ✅ 자동 포맷팅 |
| **코드 품질** | ESLint | ✅ 자동 검증 |
| **테스트 실행** | Vitest/Cypress | ✅ CI/CD 필수 |
| **PR 템플릿** | GitHub Templates | ⚠️ 수동 작성 |

---

## 🔧 도구 설정

### ESLint + Prettier 설치
```bash
npm install -D eslint prettier eslint-config-prettier
npm install -D @typescript-eslint/parser @typescript-eslint/eslint-plugin
```

### Commitlint 설치
```bash
npm install -D @commitlint/cli @commitlint/config-conventional
echo "module.exports = { extends: ['@commitlint/config-conventional'] };" > commitlint.config.js
```

### Husky (Git Hooks)
```bash
npm install -D husky
npx husky init
```

---

## 📚 참고 문서

1. **[FRONTEND_ARCHITECTURE_V3_FINAL.md](../FRONTEND_ARCHITECTURE_V3_FINAL.md)** - 전체 아키텍처
2. **[WIREFRAME_DESIGN.md](../WIREFRAME_DESIGN.md)** - UI/UX 설계
3. **[DEVELOPMENT_ROADMAP.md](../DEVELOPMENT_ROADMAP.md)** - 개발 계획

---

## 🎓 학습 자료

### Git 관련
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Git Flow](https://nvie.com/posts/a-successful-git-branching-model/)

### 프론트엔드 관련
- [Feature-Sliced Design](https://feature-sliced.design/)
- [ESLint Rules](https://eslint.org/docs/rules/)
- [Prettier Options](https://prettier.io/docs/en/options.html)

### 상태 관리
- [Pinia Documentation](https://pinia.vuejs.org/)
- [Vue 3 Composition API](https://vuejs.org/guide/extras/composition-api-faq.html)

---

## 💡 컨벤션 업데이트

컨벤션은 팀의 합의를 통해 지속적으로 개선됩니다.

**변경 프로세스**:
1. GitHub Issue로 변경 제안
2. 팀 논의 (최소 2명 동의)
3. 문서 업데이트 PR 생성
4. 리뷰 후 병합

---

**📝 이 컨벤션은 VIRTUDY 프로젝트의 품질과 협업 효율을 보장하는 핵심 문서입니다.**  
**모든 팀원은 개발 시작 전 반드시 숙지해야 합니다!** 🚀