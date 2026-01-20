# 브랜치 전략 (Branch Strategy)

## 0. 목적

- 브랜치명만 보고 **작업 목적과 범위**를 파악할 수 있게 한다.
- **Jira 이슈와 연결**하여 추적성을 높인다.
- **자동화/CI/CD**에서 쉽게 검증·관리할 수 있도록 규칙화한다.

---

## 1. 기본 규칙

브랜치 이름은 다음 형식을 따른다:

```
<prefix>/<JIRA-KEY>-<short-description>
```

- **prefix**: 작업 유형
- **JIRA-KEY**: 예) `PRJ-123`
- **short-description**: 소문자, 하이픈(-) 구분, 3~5 단어

**예시:**
- `feat/PRJ-310-meal-recommender`
- `bugfix/PRJ-455-form-validation`
- `hotfix/PRJ-999-crash-on-start`
- `docs/PRJ-150-update-api-spec`

---

## 2. Prefix 정의

| prefix | 사용 상황 |
|--------|----------|
| `feat` | 신규 기능 개발 |
| `bugfix` | 일반 버그 수정 |
| `hotfix` | 프로덕션 긴급 수정 |
| `refactor` | 리팩토링 (기능 변화 없음) |
| `docs` | 문서/주석 수정 |
| `chore` | 빌드/의존성/환경 설정 |
| `release` | 릴리스 후보 안정화 |

---

## 3. 브랜치 흐름

### 상시 브랜치

- **main** → 프로덕션 기준, 항상 배포 가능한 상태
- **dev** → 모든 개발 작업이 모이는 통합 브랜치

### 일시 브랜치

- **release/*** : `dev`에서 분기 → QA/버그 수정/문서 업데이트
  - 완료 시 `main`으로 병합 + 태깅(`vX.Y.Z`)
  - 변경 사항은 **dev에도 백머지**

- **feat/*** → Jira 티켓 단위 신규 기능

- **bugfix/*** → Jira 티켓 단위 일반 버그

- **hotfix/*** → `main`에서 바로 분기해 긴급 수정, `main`과 `dev` 양쪽에 반영

---

## 4. 머지 전략

### feat/bugfix → dev
- **PR로 병합**: Squash merge 권장 (히스토리 단순화)
- 리뷰 최소 1명 필수
- CI 파이프라인 통과 필수

### release/ → main
- **Merge commit 사용** (릴리스 히스토리 보존)
- 병합 후 태그(`vX.Y.Z`) 생성
- 동일 변경 사항을 dev에도 반영

### hotfix/ → main (+ dev)
- 빠른 병합 후 태깅
- 동일 변경 사항을 dev에도 반영

---

## 5. 보호 브랜치 설정

### Protected branches
- `main`
- `dev`
- `release/*` (패턴 보호)

### 규칙 예시
- 직접 푸시 금지 (PR 필수)
- 리뷰 최소 1–2명 승인 필요
- CI 파이프라인 통과 필수
- 머지 후 작업 브랜치는 자동 삭제

---

## 6. 자동화 & 태깅

### CI 파이프라인
- **feat/bugfix**: lint + unit test
- **dev**: lint + unit + integration test
- **release/*, main**: full test + e2e + build + 배포

### 태깅 규칙
- `main` 병합 시 `vX.Y.Z` 형식으로 태그 생성
- 예: `v1.3.0`
- Semantic Versioning 준수

---

## 7. 브랜치 생명주기

```
1. 브랜치 생성
   git checkout -b feat/PRJ-123-login-feature

2. 작업 및 커밋
   git commit -m "feat(auth): Add login form"

3. PR 생성
   - PR 템플릿 작성 (03-pull-request-rules.md 참조)
   - 리뷰 요청

4. 리뷰 & 머지
   - 리뷰어 승인
   - CI 통과
   - Squash and Merge

5. 브랜치 삭제
   - 자동 삭제 (GitHub 설정)
```

---

## 8. 예외 상황

### 긴급 핫픽스
```bash
# main에서 직접 분기
git checkout main
git checkout -b hotfix/PRJ-999-critical-bug

# 수정 후 main과 dev 양쪽에 병합
git checkout main
git merge hotfix/PRJ-999-critical-bug
git tag v1.2.1

git checkout dev
git merge hotfix/PRJ-999-critical-bug
```

### 장기 feature 브랜치
- 2주 이상 걸리는 큰 기능은 `feature/*` 브랜치에서 작업
- 주기적으로 dev의 변경사항을 rebase하여 동기화

---

## 9. 체크리스트

브랜치 생성 전:
- [ ] Jira 티켓 번호 확인
- [ ] 브랜치명 규칙 준수 (`<prefix>/<JIRA-KEY>-<description>`)
- [ ] dev 브랜치 최신화 (`git pull origin dev`)

PR 생성 전:
- [ ] 로컬 테스트 통과
- [ ] 커밋 메시지 규칙 준수 (02-commit-message.md)
- [ ] PR 템플릿 작성 (03-pull-request-rules.md)

---

**📌 이 브랜치 전략은 Git 충돌을 최소화하고 협업 효율을 극대화하기 위해 설계되었습니다.**  
**모든 팀원은 반드시 이 규칙을 따라야 합니다!**