# 🛠️ 개발 환경 설정 가이드 (Environment Setup Guide)

## 🚨 문제점 (이 가이드가 필요한 이유)
지금까지 우리는 **로컬(내 컴퓨터)**과 **배포(서버)** 환경을 오갈 때마다 코드를 수동으로 수정(주석 해제/주석 처리)했습니다.  
이로 인해 다음과 같은 문제가 계속 발생했습니다:
1. 실수로 배포 서버 주소를 로컬 설정 파일에 커밋함.
2. 다른 팀원이 `git pull`을 받으면 갑자기 로컬 실행이 안 됨 (Connection Refused).
3. `vite.config.ts` 등의 설정 파일 충돌(Merge Conflict)이 빈번하게 발생.

---

## ✅ 해결 방안 (Standard)
우리는 이제 **코드를 수정하지 않고도** 실행 명령어만으로 환경을 구분하도록 설정을 분리합니다.

### 1. 프론트엔드 (`FE/virtudy-frontend`)
**환경 변수 (`.env`)**를 사용하여 자동으로 주소를 바꿉니다.

- **로컬 개발 (`dev`)**: `pnpm run dev` 실행 시 `.env.development.local` 파일을 사용
- **배포 빌드 (`build`)**: `pnpm run build` 실행 시 `.env.production.local` 파일을 사용

> **👉 팀원들이 해야 할 일:**  
> 각자 로컬의 `.env.development.local` 파일을 아래와 같이 한 번만 생성/수정하고,  
> **다시는 `RoomManager.ts`나 `vite.config.ts`의 URL 코드를 건드리지 마세요.**

#### 📄 `.env.development.local` (로컬 개발용)
```ini
VITE_LIVEKIT_URL=ws://127.0.0.1:7880
VITE_SOCKET_URL=http://127.0.0.1:8081/ws
VITE_AI_SOCKET_URL=ws://127.0.0.1:8000/ws/analysis
```

#### 📄 `.env.production.local` (배포용 - 참고만 하세요)
```ini
VITE_LIVEKIT_URL=wss://i14a703.p.ssafy.io
VITE_SOCKET_URL=https://i14a703.p.ssafy.io/ws
VITE_AI_SOCKET_URL=wss://i14a703.p.ssafy.io/ws/analysis
```

---

### 2. 백엔드 (`BE`)
**Spring Profile**을 사용하여 설정을 분리합니다.

- **로컬 모드**: `local` 프로필로 실행 (`application-local.yaml` 사용)
- **배포 모드**: `dev` 프로필로 실행 (`application-dev.yaml` 사용)

> **👉 팀원들이 해야 할 일:**  
> `src/main/resources/application-local.yaml` 파일에는 반드시 **로컬 주소(127.0.0.1)**만 적혀 있어야 합니다. (배포 주소 금지!)

#### 로컬에서 실행하는 법
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```
(또는 프로젝트 루트에 있는 `start_all.bat` 스크립트를 사용하세요.)

#### 배포(Docker) 시 실행되는 법
Docker/Jenkins 파이프라인에서 이미 다음과 같이 설정되어 있습니다:
```bash
java -jar -Dspring.profiles.active=dev app.jar
```

---

### 3. AI 서버 (`AI/concentration_monitor`)
**필수 라이브러리 설치**  
AI 서버 실행 전, 필요한 패키지들이 설치되어 있는지 확인해야 합니다.

```bash
cd AI/concentration_monitor
pip install -r requirements.txt
pip install livekit-api # (혹시 누락될 경우를 대비해 명시)
```
AI 서버 실행:
```bash
python server.py
# 또는
start_all.bat 사용
```

---

## 🚀 요약표

| 구분 | 로컬 실행 명령어 | 배포(빌드) 명령어 |
| :--- | :--- | :--- |
| **Frontend** | `pnpm run dev` | `pnpm run build` |
| **Backend** | `... --spring.profiles.active=local` | `... -Dspring.profiles.active=dev` |
| **AI** | `python server.py` (또는 start_all.bat) | (Backend가 자동 실행 관리) |

**서로의 개발 환경을 깨뜨리지 않도록 이 규칙을 꼭 지켜주세요!** 🙏
