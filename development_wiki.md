# AI-LiveKit 연동 개발 가이드 (Development Wiki)

이 문서는 실시간 집중도 분석을 위한 AI Bot과 프론트엔드 간의 연동 내역, 수정 파일, 실행 방법을 정리한 문서입니다.

---

## 1. 아키텍처 개요
*   **목표**: Python AI가 LiveKit 룸에 참여자(Bot)로 입장하여 유저의 영상을 실시간으로 분석하고, 결과를 프론트엔드로 전송.
*   **데이터 흐름**: `AI Script` (영상 구독/분석) -> `LiveKit Data Message` (전송) -> `Frontend` (수신/시각화)
*   **핵심 변경**: 입모양/눈깜빡임 등 과도한 데이터 전송을 제거하고, 핵심 지표(**집중도 점수, 집중 상태**) 위주로 경량화.

---

## 2. 주요 파일 및 수정 내역

### 2.1 AI (Python) - `AI/concentration_monitor/`

#### 📄 `run_livekit.py` (신규 핵심 로직)
*   **역할**: LiveKit 클라이언트로서 유저 비디오를 수신하고 AI 분석 수행.
*   **주요 코드 변경**:
    1.  **비디오 프레임 변환**: LiveKit SDK 최신 버전의 `VideoFrame` 호환성 문제 해결.
        ```python
        # 기존 buffer 속성 오류 해결 -> convert() 메서드 사용
        rgba_buffer = real_frame.convert(rtc.VideoBufferType.RGBA)
        arr = np.frombuffer(rgba_buffer.data, dtype=np.uint8)
        ```
    2.  **기능 단순화**: `ExtendedFeatureExtractor`(입모양 계산) 로직 제거 및 기본 `FeatureExtractor` 사용.

#### 📄 `gen_token_manual.py` (신규 유틸리티)
*   **역할**: 시차 문제(`nbf` claim)로 인한 `401 Unauthorized` 에러를 해결하기 위해 수동으로 JWT 토큰 생성.
*   **특징**: `nbf`(Not Before) 시간을 현재보다 과거로 설정하여 즉시 사용 가능하도록 함.

---

### 2.2 Frontend (Vue.js/TS) - `FE/virtudy-frontend/`

#### 📄 `src/features/study-room/logic/useStudyRoom.ts`
*   **문제 해결**:
    1.  **AI 데이터 폭주**: 채팅창에 AI 데이터가 도배되는 문제 해결.
        ```typescript
        roomManager.onMessage((payload) => {
            if (payload && payload.category) return; // AI 데이터는 채팅에서 제외
            messages.value.push(payload);
        });
        ```
    2.  **리스너 삭제 문제 (Race Condition)**: 방 입장 시 `removeAllListeners`를 호출하여 AI 핸들러가 끊기는 문제 해결 (해당 라인 주석 처리).

#### 📄 `src/shared/api/livekit/RoomManager.ts`
*   **기능 추가**: LiveKit의 `RoomEvent.DataReceived` 이벤트를 수신하여 `messageListeners`에게 전달하는 브리지 역할 구현.
    ```typescript
    this.room.on(RoomEvent.DataReceived, (payload, ...) => {
        const data = JSON.parse(new TextDecoder().decode(payload));
        this.messageListeners.forEach(listener => listener(data));
    });
    ```

#### 📄 `src/features/study-room/logic/useAiHandler.ts`
*   **변경**: `STATUS`, `SCORE` 외 불필요한 `MOUTH`, `BLINK` 핸들러 제거. 디버깅용 로그(`Received: ...`) 추가.

#### 📄 `src/features/study-room/pages/StudyRoomPage.vue`
*   **UI 정리**: 작동하지 않거나 불필요해진 '입모양', '눈 상태' UI 요소 제거.

---

## 3. 실행 가이드 (How to Run)

### 프로세스 요약
1.  **프론트엔드**: 스터디룸 입장 (먼저 입장해서 방을 생성해야 함)
2.  **토큰 발급**: 현재 방의 ID(`roomId`)를 복사하여 AI Bot용 토큰 생성
3.  **AI 실행**: 발급된 토큰으로 Python 스크립트 실행

### 단계별 명령어

#### Step 1: 룸 ID 확인
프론트엔드 URL이나 콘솔 로그에서 현재 `roomId` 확인 (예: `13e11550-...`)

#### Step 2: AI Bot 토큰 생성
`AI/concentration_monitor/` 경로에서 실행:
```bash
python gen_token_manual.py --room "ROOM_UUID" --key "APIteMFGyZJPUqY" --secret "22IQPqfgWwSuHSziBe9Cxe9HXOmehSIeKtK7xEvLUJfE"
```
*   결과: `token_utf8.txt` 파일에 토큰이 저장되거나 터미널에 출력됨.

#### Step 3: AI Bot 실행
위에서 얻은 토큰(`TOKEN_STRING`)을 사용하여 실행:
```bash
python run_livekit.py --url "wss://virtudy-vcm6hufs.livekit.cloud" --token "TOKEN_STRING"
```

---

## 4. 트러블슈팅

*   **`401 Unauthorized`**: 토큰 만료 또는 시차 문제. `gen_token_manual.py`로 토큰을 새로 발급받으세요.
*   **`AttributeError: VideoFrame...`**: LiveKit SDK 버전 불일치. `pip install livekit>=0.7.1` 확인 및 `run_livekit.py`의 `convert` 로직 확인.
*   **연결되었는데 데이터 안 옴**:
    *   AI 로그에 `Subscribed to Video Track`이 떴는지 확인. (안 떴으면 프론트엔드가 카메라를 안 켰거나 다른 방임)
    *   프론트엔드 콘솔에 `[useAiHandler] Received`가 뜨는지 확인.

---
**작성일**: 2026-01-29
**작성자**: Antigravity AI
