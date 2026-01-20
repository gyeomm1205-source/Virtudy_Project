# 커밋 메시지 컨벤션 (Commit Message Convention)

## 1. 목표

* **명확한 히스토리**: 누가, 언제, 무엇을, 왜 변경했는지 쉽게 파악할 수 있도록 합니다.
* **자동화 기반**: 커밋 히스토리를 기반으로 버전 릴리즈 노트(Changelog)를 자동으로 생성합니다.
* **효율적인 협업**: 동료 리뷰어가 변경 사항의 의도를 빠르게 이해하고 리뷰할 수 있도록 돕습니다.

---

## 2. 기본 구조

모든 커밋 메시지는 아래와 같은 구조를 따릅니다. 헤더는 필수이며, 본문과 푸터는 선택 사항입니다.

```
<type>(<scope>): <subject>
<-- 빈 줄 -->
<body>
<-- 빈 줄 -->
<footer>
```

---

## 3. 헤더 (Header) 상세 규칙

헤더는 `type`, `scope`, `subject` 세 부분으로 구성됩니다.

### 3.1. `type`: 커밋의 종류

| 타입 (Type) | 설명 |
|:-----------|:-----|
| **`feat`** | 새로운 기능 추가 (a new feature) |
| **`fix`** | 버그 수정 (a bug fix) |
| **`docs`** | 문서 변경 (README, API 문서 등) |
| **`style`** | 코드 스타일 수정 (포맷팅, 세미콜론 등 기능 변경 없음) |
| **`refactor`** | 코드 리팩토링 (기능 변경 없이 코드 구조를 개선) |
| **`test`** | 테스트 코드 추가 또는 수정 |
| **`chore`** | 기타 잡무 (빌드, 패키지 매니저 설정 등 코드와 무관한 변경) |
| **`ci`** | CI/CD 관련 설정 변경 |
| **`perf`** | 성능 개선 |
| **`revert`** | 이전 커밋을 되돌리는 작업 (revert) |

### 3.2. `scope` (선택 사항): 변경 범위

- 커밋이 영향을 미치는 부분(모듈, 페이지, 컴포넌트 등)을 명시합니다.
- **예시**: `auth`, `ui`, `api`, `hooks`, `login-page`

### 3.3. `subject`: 변경 내용 요약

- **반드시 동사 원형을 사용한 명령문**으로 작성합니다. (`Add`, `Fix`, `Change`)
- 첫 글자는 **대문자**로 작성하고, 50자 이내로 간결하게 요약합니다.
- 문장 끝에 **마침표(.)를 찍지 않습니다.**

---

## 4. 본문 (Body) 및 푸터 (Footer)

### 본문 (Body)
- "왜" 이 변경을 했는지에 대한 상세한 설명을 작성합니다.
- 선택 사항이지만, 복잡한 변경사항의 경우 작성을 권장합니다.

### 푸터 (Footer)
- 관련 Jira 티켓(`Resolves: FBC-123`)이나 중대한 변경 사항(`BREAKING CHANGE:`)을 명시합니다.

---

## 5. WIP (Work In Progress) 커밋 워크플로우

`WIP`는 아직 작업이 진행 중인 **임시 커밋**을 의미합니다. `main` 브랜치의 히스토리를 깨끗하게 유지하기 위해, `commitlint`는 `WIP` 커밋을 의도적으로 차단합니다. 따라서 아래 워크플로우를 반드시 따라야 합니다.

### 1단계: 자유롭게 `WIP` 커밋 사용

기능 브랜치에서 작업하는 동안, 진행 상황을 자유롭게 `WIP:` 접두사를 붙여 커밋합니다.

```bash
git commit -m "WIP: Start implementing login form UI"
git commit -m "WIP: Add email validation logic"
```

### 2단계: PR 생성 전, 커밋 합치기 (Squash)

기능 개발이 완료되면, PR을 생성하기 전에 흩어져 있던 `WIP` 커밋들을 `git rebase`를 이용해 **하나의 의미 있는 커밋**으로 합칩니다.

```bash
# 가장 최근의 2개 커밋을 합치기
git rebase -i HEAD~2
```

### 3단계: 최종 커밋 메시지 작성

`rebase` 편집기에서 합칠 커밋들을 `squash` 또는 `fixup`으로 지정한 후, 최종 커밋 메시지를 **컨벤션에 맞게** 다시 작성합니다.

```
feat(auth): Add login form with email validation

- Implemented the basic UI structure for the login form.
- Added real-time email format validation to enhance user experience.
```

### 4단계: 원격 브랜치에 강제 푸시

로컬 히스토리가 변경되었으므로, 원격 브랜치에 강제 푸시(`--force-with-lease`)하여 업데이트합니다.

```bash
git push --force-with-lease
```

이제 `WIP` 과정은 사라지고, `main` 브랜치에 남을 깔끔한 최종 커밋만 남게 됩니다.

---

## 6. 종합 예시

### 기능 추가
```
feat(auth): Add Google social login feature
```

### 버그 수정 (상세 내용 포함)
```
fix(api): Correct user data fetching error

The user data was not being fetched correctly after login due to an incorrect API endpoint.

Closes: FBC-210
```

### 되돌리기 커밋
```
revert: feat(auth): Add Google social login feature

This reverts commit a1b2c3d4e5f6g7h8i9j0.

Reason: This feature caused a critical authentication loop on Safari browsers.
```

### 중대한 변경 사항
```
feat(api): Change user authentication flow

BREAKING CHANGE: The login API endpoint has changed from /api/v1/login to /api/v2/auth/login.
All clients must update their API calls accordingly.
```

---

## 7. Commitlint 설정

### 설치
```bash
npm install -D @commitlint/cli @commitlint/config-conventional
```

### 설정 파일 (commitlint.config.js)
```javascript
module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      [
        'feat',
        'fix',
        'docs',
        'style',
        'refactor',
        'test',
        'chore',
        'ci',
        'perf',
        'revert'
      ]
    ],
    'subject-case': [2, 'always', 'sentence-case']
  }
};
```

### Husky 연동
```bash
npx husky add .husky/commit-msg 'npx --no -- commitlint --edit "$1"'
```

---

## 8. 체크리스트

커밋 전:
- [ ] type이 올바른가? (feat, fix, docs 등)
- [ ] scope가 명확한가? (auth, ui, api 등)
- [ ] subject가 50자 이내인가?
- [ ] subject가 명령문인가? (Add, Fix, Change)
- [ ] 본문이 필요한 경우 작성했는가?
- [ ] Jira 티켓 번호를 푸터에 명시했는가?

---

**📌 이 커밋 메시지 컨벤션은 프로젝트 히스토리를 명확하고 일관되게 유지하기 위한 필수 규칙입니다.**  
**모든 커밋은 반드시 이 규칙을 따라야 하며, Commitlint가 자동으로 검증합니다!**