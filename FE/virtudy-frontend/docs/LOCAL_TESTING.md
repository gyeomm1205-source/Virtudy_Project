# 🎥 로컬 LiveKit 멀티유저 테스트 가이드

이 문서는 로컬 개발 환경에서 백엔드 연동 없이 **여러 명의 사용자가 한 방에 접속하는 상황을 테스트**하는 방법과, 관련하여 수정된 코드 내용을 설명합니다.

---

## 🛑 문제 상황 (Background)

초기 개발 환경에서는 다음과 같은 문제가 있었습니다:
1. **User ID 중복**: `livekit-server`는 동일한 `identity`(User ID)로 접속 시도 시, 기존 연결을 즉시 끊어버립니다 (Duplicate Identity Kick-out).
2. **토큰 의존**: 기존 로직은 항상 백엔드에서 발급받은 JWT 토큰을 요구하여, 서로 다른 계정으로 테스트하려면 매번 로그아웃/로그인을 반복하거나 실제 DB 계정이 여러 개 필요했습니다.
3. **에러 메시지 미흡**: 연결이 끊어질 때 원인을 알 수 없어 디버깅이 어려웠습니다.

---

## ✅ 해결 방법 (Changes)

프론트엔드 코드(`virtudy-frontend`)를 수정하여 로컬 테스트 유연성을 확보했습니다.

### 1. URL 파라미터로 User ID 강제 지정 (`StudyRoomPage.vue`)
URL 뒤에 `?userId=...` 파라미터가 있으면, 로그인된 정보보다 우선하여 해당 ID를 사용하도록 변경했습니다.
```typescript
// StudyRoomPage.vue (변경 전)
const userId = authStore.userId || ...;

// StudyRoomPage.vue (변경 후)
// URL 쿼리 파라미터가 최우선 순위
const userId = (route.query.userId as string) || authStore.userId || `guest-${...}`;
```

### 2. 로컬 토큰 자동 생성 (`RoomManager.ts`)
외부에서 주입된 토큰(`token`)이 없더라도, 로컬 환경(`LocalTokenGenerator`)을 이용해 즉석에서 토큰을 생성하고 입장하도록 변경했습니다.
```typescript
// RoomManager.ts
const { LocalTokenGenerator } = await import('@/shared/lib/LocalTokenGenerator');
// 토큰이 없으면 LocalTokenGenerator로 생성한 토큰 사용 (백엔드 없이 동작 가능)
const finalToken = token || await LocalTokenGenerator.generateToken(roomId, userId);

await this.connectLiveKit(finalToken);
```

### 3. 연결 해제 원인 로깅
LiveKit 연결이 끊어질 때 구체적인 원인(`Reason`)을 로그에 남기도록 수정하여, 중복 로그인 문제를 식별하기 쉽게 했습니다.
```typescript
this.room.on(RoomEvent.Disconnected, (reason) => {
    console.warn('[LiveKit] 연결이 끊어졌습니다. Reason:', reason);
});
```

---

## 🧪 테스트 방법 (How to Test)

이제 **백엔드 서버가 켜져 있지 않아도**, 혹은 **계정이 하나뿐이어도** 여러 명의 화상 채팅을 테스트할 수 있습니다.

### 준비물
- Frontend 실행: `npm run dev` (Localhost:5173)
- LiveKit Server 실행: `livekit-server --dev` (Docker 사용 시 `docker run ...`)

### 접속 절차

#### 1️⃣ 첫 번째 사용자 (방장)
일반적인 방법으로 방을 생성하거나 입장합니다. (예: 로그인 후 입장)
- URL: `http://localhost:5173/study/room/ROOM_ID?token=EXISTING_TOKEN...`

#### 2️⃣ 두 번째 사용자 (테스트 유저 A)
**시크릿 창(Incognito)** 또는 다른 브라우저를 엽니다. 토큰을 지우고 `userId`를 임의로 지정합니다.
- URL: `http://localhost:5173/study/room/ROOM_ID?userId=userA`
- **주의:** `token=...` 파라미터는 지우세요. (복사한 주소에 기존 토큰이 남아있으면 중복 로그인으로 첫 번째 창이 튕깁니다.)

#### 3️⃣ 세 번째 사용자 (테스트 유저 B)
또 다른 탭이나 브라우저에서 `userId`만 다르게 입력합니다.
- URL: `http://localhost:5173/study/room/ROOM_ID?userId=userB`

---

## 📁 관련 파일

| 파일 경로 | 설명 |
| --- | --- |
| `src/features/study-room/pages/StudyRoomPage.vue` | URL 쿼리 파싱 및 `userId` 우선순위 로직 추가 |
| `src/shared/api/livekit/RoomManager.ts` | 토큰 부재 시 로컬 토큰 생성 허용, Disconnect 로그 강화 |
| `src/shared/lib/LocalTokenGenerator.ts` | (기존 파일) 로컬 테스트용 JWT 토큰 생성 유틸리티 |

이제 팀원들도 위 방법을 통해 손쉽게 다자간 화상 테스트를 진행할 수 있습니다. 🚀
