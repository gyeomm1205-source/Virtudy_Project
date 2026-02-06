import { Room, RoomEvent, RemoteParticipant, RemoteTrackPublication, RemoteTrack, LocalVideoTrack, Track } from 'livekit-client';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// 백엔??URL ?�정 (?�경 변???�는 ?�수�?관�?권장)
// 백엔??URL ?�정 (?�경 변???�는 ?�수�?관�?권장)
// const LIVEKIT_URL = import.meta.env.VITE_LIVEKIT_URL || 'ws://127.0.0.1:7880';
// const SOCKET_URL = import.meta.env.VITE_SOCKET_URL || 'http://127.0.0.1:8081/ws';

// // [추�?] AI ?�버 ?�소�?주소 (FastAPI ??AI ?�버???�소�??�드?�인??
// const AI_SOCKET_URL = import.meta.env.VITE_AI_SOCKET_URL || 'ws://127.0.0.1:8000/ws/analysis';

// 백엔드 URL 설정 (환경 변수 또는 상수로 관리 권장)
// const LIVEKIT_URL = import.meta.env.VITE_LIVEKIT_URL || 'ws://127.0.0.1:7880'; // 환경변수 우선 사용
const LIVEKIT_URL = import.meta.env.VITE_LIVEKIT_URL || 'ws://127.0.0.1:7880';

// const SOCKET_URL = 'http://127.0.0.1:8081/ws'; // 백엔드 요구사항: 8081포트로 직접 연결
const SOCKET_URL = import.meta.env.VITE_SOCKET_URL || 'http://127.0.0.1:8081/ws';

// [추�?] AI ?�버 ?�소�?주소 (FastAPI ??AI ?�버???�소�??�드?�인??
// const AI_SOCKET_URL = 'ws://127.0.0.1:8000/ws/analysis';
const AI_SOCKET_URL = import.meta.env.VITE_AI_SOCKET_URL || 'ws://127.0.0.1:8000/ws/analysis';

// 3. [AI 서버] API 주소 (HTTP 요청용 -> HTTPS)
// Nginx 설정상 /fastapi 로 들어오는 요청을 AI 서버로 보내게 되어 있다고 가정합니다.
const AI_API_URL = import.meta.env.VITE_AI_API_URL ;

export class RoomManager {
    private static instance: RoomManager;
    private room: Room | null = null;
    private stompClient: Client | null = null;
    private aiSocket: WebSocket | null = null; // [추�?] AI??직통 ?�켓
    private roomId: string = '';
    private userId: string = ''; // [추�?] userId ?�??

    private constructor() { }

    static getInstance(): RoomManager {
        if (!RoomManager.instance) {
            RoomManager.instance = new RoomManager();
        }
        return RoomManager.instance;
    }

    public getRoom(): Room | null {
        return this.room;
    }
    

    // 1. ?�합 ?�결 ?�수 (?�장 API -> LiveKit ?�결 -> ?�켓 ?�결)
    async joinStudyRoom(roomId: string, userId: string, token?: string) {
        this.roomId = roomId;
        this.userId = userId; // [추�?] user ID ?�??

        try {
            // [?�정] ?�론?�엔??로컬 ?�큰 ?�성�??�용 (백엔??미구???�??
            const { LocalTokenGenerator } = await import('@/shared/lib/LocalTokenGenerator');
            const liveKitToken = await LocalTokenGenerator.generateToken(roomId, userId);

            // ?�큰 결정: ?�자�?받�? ?�큰???�으�?로컬 ?�성 ?�큰 ?�용
            const finalToken = token || liveKitToken;

            const accessToken = localStorage.getItem('accessToken') || '';

            console.log(`[RoomManager] ?�큰 ?�인 ?�료 (${token ? 'External' : 'Local'}). LiveKit ?�결???�작?�니??`);

            // 1-2. LiveKit ?�결 (미디???�레??
            await this.connectLiveKit(finalToken);

            // 1-3. WebSocket ?�결 (컨트�??�레??
            this.connectWebSocket(accessToken);
            // [추�?]1-4. ?�켓 직통 ?�결 (집중???�이??
            this.connectAISocket();
            
            // [추가] 1-5. 봇(AI) 소환 요청 (POST)
            // 방에 입장했으니, 이제 분석 봇을 투입시킵니다.
            await this.requestBotJoin(finalToken);

            console.log(`[RoomManager] 방 ${roomId} 입장 완료`);
            console.log(`[RoomManager] 방 ${roomId} 입장 및 AI 봇 호출 완료`);
        } catch (error) {
            console.error('[RoomManager] �??�장 ?�패:', error);
            this.leaveRoom(); // ?�패 ???�리
            throw error;
        }
    }

    // 2-1. 가??카메???�랙 ?�성 (AI가 카메???�유 ???�용)
    private async createVirtualVideoTrack(): Promise<LocalVideoTrack> {
        const canvas = document.createElement('canvas');
        canvas.width = 640;
        canvas.height = 480;
        const ctx = canvas.getContext('2d');
        if (ctx) {
            ctx.fillStyle = '#000000';
            ctx.fillRect(0, 0, 640, 480);
            ctx.fillStyle = '#ffffff';
            ctx.font = '30px Arial';
            ctx.fillText('AI Camera In Use', 200, 240);
        }
        const stream = canvas.captureStream(30);
        const [videoTrack] = stream.getVideoTracks();
        if (!videoTrack) {
            throw new Error('가??카메???�트림에??비디???�랙??찾을 ???�습?�다.');
        }
        return new LocalVideoTrack(videoTrack);
    }

    // 2. LiveKit ?�결 로직
    private async connectLiveKit(token: string) {
        this.room = new Room({
            // ?�디??비디???�동 캡처 ?�정
            adaptiveStream: true,
            dynacast: true,
        });

        // LiveKit ?�벤??리스???�록
        this.room.on(RoomEvent.TrackSubscribed, (track, publication, participant) => {
            this.handleTrackSubscribed(track, publication, participant);
        });

        this.room.on(RoomEvent.TrackUnsubscribed, (track, publication, participant) => {
            this.handleTrackUnsubscribed(track, publication, participant);
        });

        this.room.on(RoomEvent.ParticipantConnected, (participant) => {
            this.handleParticipantConnected(participant);
        });

        this.room.on(RoomEvent.ParticipantDisconnected, (participant) => {
            this.handleParticipantDisconnected(participant);
        });

        this.room.on(RoomEvent.Disconnected, (reason) => {
            console.warn('[LiveKit] ?�결???�어졌습?�다. Reason:', reason);
        });

        this.room.on(RoomEvent.DataReceived, (payload, participant, _kind, _topic) => {
            const strData = new TextDecoder().decode(payload);
            try {
                const data = JSON.parse(strData);
                console.log(`[LiveKit] ?�이???�신 (${participant?.identity}):`, data);
                // [Fix] Filter AI bot broadcast by participantId
                if (data && data.category) {
                    const targetId = data.participantId;
                    if (!targetId || targetId !== this.userId) {
                        return;
                    }
                }
// 기존 메시지 리스?�에�??�달 (useAiHandler ?�에??처리)

                // [?�정] sender ?�보(participant.identity)�??�께 ?�달
                this.messageListeners.forEach(listener => listener(data, participant?.identity));
            } catch (e) {
                console.warn('[LiveKit] ?�이???�싱 ?�패:', strData);
            }
        });

        try {
            await this.room.connect(LIVEKIT_URL, token);
            console.log('[LiveKit] ?�버 ?�결 ?�공');

            await this.room.localParticipant.setMicrophoneEnabled(false);

            // [수정] AI 봇(서버) 방식을 사용하므로 브라우저가 실제 카메라를 송출해야 함
            // 가상 카메라(Avatar)는 로컬 UI에서 처리하고, 송출은 실제 카메라로 해야 AI가 분석 가능
            console.log('[LiveKit] 실제 카메라를 고화질(720p)로 시작합니다.');
            await this.room.localParticipant.setCameraEnabled(true, {
                resolution: {
                    width: 1280,
                    height: 720,
                    frameRate: 30,
                }
            });

            // Legacy(origin/fe) Logic:
            // await this.room.localParticipant.publishTrack(virtualTrack, { source: Track.Source.Camera });

        } catch (e) {
            console.error(e);
            throw e;
        }
    }

    // [추가] AI 봇 소환 메서드 (POST 요청)
    private async requestBotJoin(token: string) {
        try {
            console.log("[Bot] AI 봇 투입 요청 중...");
            
            // 백엔드의 JoinRequest 모델에 맞춰 데이터 전송
            const response = await fetch(`${AI_API_URL}/bot/join`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    url: LIVEKIT_URL, // 봇이 접속할 LiveKit 주소
                    token: token,     // 봇이 사용할 토큰 (혹은 봇용 토큰을 따로 발급받기도 함)
                    room_id: this.roomId
                }),
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`봇 투입 실패: ${response.status} ${errorText}`);
            }

            const result = await response.json();
            console.log("[Bot] 봇 투입 성공:", result);

        } catch (error) {
            // 봇이 실패해도 사용자는 입장은 유지할지, 에러를 띄울지 정책 결정 필요
            console.error("[Bot] 요청 중 에러 발생:", error);
        }
    }

    // 3. WebSocket(SockJS+Stomp) 연결 로직

    // private connectWebSocket() {
    private connectWebSocket(token: String) {
        this.stompClient = new Client({
            webSocketFactory: () => new SockJS(SOCKET_URL),
            connectHeaders: {
                memberId: this.userId,
                roomId: this.roomId,
                // [중요] JWT ?�큰 추�? (Bearer 공백 주의)
                Authorization: `Bearer ${token}`,
            },
            debug: (str) => {
                // console.log(`[Stomp] ${str}`); // ?�버�??�에�?켜기 (로그 ?�무 많음)
            },
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('[Stomp] ?�켓 ?�결 ?�공');
                this.stompClient?.subscribe(`/sub/room/${this.roomId}`, (message) => {
                    const payload = JSON.parse(message.body);
                    this.handleSocketMessage(payload);
                });
            },
            onStompError: (frame) => {
                console.error('[Stomp] ?�러 발생:', frame.headers['message']);
            },
        });

        this.stompClient.activate();
    }


    // [추가] 1-4. AI 서버 소켓 연결 (직통)
    private connectAISocket() {
        // AI ?�버??보낼 주소 (?? 방ID?� ?��?ID�?경로???�함)
        const url = `${AI_SOCKET_URL}/${this.roomId}/${this.userId}`;

        console.log(`[AI-Socket] ?�결 ?�도: ${url}`);
        this.aiSocket = new WebSocket(url);

        this.aiSocket.onopen = () => {
            console.log("?�� [AI-Socket] ?�결 ?�공!");
        };

        this.aiSocket.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                // AI가 보낸 ?�이?��? 공통 ?�들?�로 ?��?
                const payload = {
                    type: 'AI_EVENT',
                    data: {
                        eventType: data.eventType || data.status,
                        value: data.value
                    }
                };
                this.handleSocketMessage(payload);
            } catch (error) {
                console.error("[AI-Socket] ?�이???�싱 ?�류:", error);
            }
        };

        this.aiSocket.onclose = () => {
            console.log("?�� [AI-Socket] ?�결 종료");
        };

        this.aiSocket.onerror = (error) => {
            console.error("?�� [AI-Socket] ?�러:", error);
        };
    }

    // ?�랙 구독 ?�들??(?�면??비디???�시)
    private handleTrackSubscribed(
        track: RemoteTrack,
        _publication: RemoteTrackPublication,
        participant: RemoteParticipant
    ) {
        if (track.kind === 'video' || track.kind === 'audio') {
            console.log(`[LiveKit] ?�랙 ?�신: ${participant.identity} (${track.kind})`);
            // 리스?�들?�게 ?�림
            this.trackListeners.forEach(listener => listener(track, participant));
        }
    }

    // ?�랙 구독 ?�제 ?�들??
    private handleTrackUnsubscribed(
        track: RemoteTrack,
        _publication: RemoteTrackPublication,
        participant: RemoteParticipant
    ) {
        console.log(`[LiveKit] ?�랙 ?�제: ${participant.identity} (${track.kind})`);
        this.trackCleanupListeners.forEach(listener => listener(track, participant));
    }

    // 참가자 입장 핸들러
    private handleParticipantConnected(participant: RemoteParticipant) {
        console.log(`[LiveKit] 참가자 입장: ${participant.identity}`);
        this.participantConnectedListeners.forEach(listener => listener(participant));
    }

    // 참가자 퇴장 핸들러
    private handleParticipantDisconnected(participant: RemoteParticipant) {
        console.log(`[LiveKit] 참가자 퇴장: ${participant.identity}`);
        this.participantDisconnectedListeners.forEach(listener => listener(participant));
    }

    // 메시지 리스너 관리
    // [수정] listener 타입 변경: payload + senderId
    private messageListeners: ((payload: any, senderId?: string) => void)[] = [];
    // ?�랙 리스??관�?
    private trackListeners: ((track: RemoteTrack, participant: RemoteParticipant) => void)[] = [];
    private trackCleanupListeners: ((track: RemoteTrack, participant: RemoteParticipant) => void)[] = [];
    // 참가자 입/퇴장 리스너 관리
    private participantConnectedListeners: ((participant: RemoteParticipant) => void)[] = [];
    // 참가자 퇴장 리스너 관리
    private participantDisconnectedListeners: ((participant: RemoteParticipant) => void)[] = [];

    // 메시지 ?�신 ?�벤???�록
    onMessage(callback: (payload: any, senderId?: string) => void) {
        this.messageListeners.push(callback);
    }

    // ?�랙 ?�신 ?�벤???�록
    onTrackSubscribed(callback: (track: RemoteTrack, participant: RemoteParticipant) => void) {
        this.trackListeners.push(callback);
    }

    // ?�랙 ?�제 ?�벤???�록
    onTrackUnsubscribed(callback: (track: RemoteTrack, participant: RemoteParticipant) => void) {
        this.trackCleanupListeners.push(callback);
    }

    // 참가자 입장 이벤트 등록
    onParticipantConnected(callback: (participant: RemoteParticipant) => void) {
        this.participantConnectedListeners.push(callback);
    }

    // 참가자 퇴장 이벤트 등록
    onParticipantDisconnected(callback: (participant: RemoteParticipant) => void) {
        this.participantDisconnectedListeners.push(callback);
    }

    // 모든 리스너 제거 (중복 방지용)
    removeAllListeners() {
        this.messageListeners = [];
        this.trackListeners = [];
        this.trackCleanupListeners = [];
        this.participantConnectedListeners = [];
        this.participantDisconnectedListeners = [];
    }

    // ?�켓 메시지 ?�들??(?�태 ?�데?�트)
    private handleSocketMessage(payload: any) {
        console.log('[Socket] 메시지 ?�신:', payload);
        // ?�록??리스?�들?�게 ?�림 (SYSTEM 메시지?��?�?senderId??undefined 처리)
        this.messageListeners.forEach(listener => listener(payload, undefined));
    }

    // �??��?�?(Cleanup)
    leaveRoom(disconnectHeaders?: Record<string, string>) {
        this.room?.disconnect();
        this.room = null;
        // [추�?] AI ?�켓 종료
        if (this.aiSocket) {
            this.aiSocket.close();
            this.aiSocket = null;
        }
        if (this.stompClient) {
            if (disconnectHeaders) {
                this.stompClient.disconnectHeaders = disconnectHeaders;
            }
            this.stompClient.deactivate();
        }
        this.stompClient = null;
        this.messageListeners = []; // 리스??초기??
        console.log('[RoomManager] �??�장 �?리소???�리 ?�료');
    }

    // [추�?] 4. LiveKit ?�이???�송 (Broadcast)
    // topic: ?�이??주제 (?? 'AI_STATUS')
    // data: ?�송??객체
    async sendLiveKitData(topic: string, data: any) {
        // [Fix] Check ConnectionState.Connected explicitly
        if (!this.room || !this.room.localParticipant || this.room.state !== 'connected') { // String literal check due to enum import complexity or check LiveKit docs
            // LiveKit RoomState: 'disconnected' | 'connected' | 'reconnecting' | 'connecting'
            console.warn(`[RoomManager] LiveKit 미연�??�태(${this.room?.state})???�이?��? 보낼 ???�습?�다.`, topic);
            return;
        }

        try {
            const strData = JSON.stringify({ topic, ...data });
            const encoder = new TextEncoder();
            const payload = encoder.encode(strData);

            await this.room.localParticipant.publishData(payload, {
                reliable: true,
            });
            console.log(`?�� [LiveKit-Broadcast] ${topic} ?�송 ?�료`, data);
        } catch (e) {
            console.warn(`[LiveKit-Broadcast] ?�송 ?�패 (?�시???�류 가?�성):`, e);
            // Error explicitly handled, no need to throw
        }
    }

    // ?�어 메시지 ?�송 ?�퍼
    sendControlMessage(type: string, data: any) {
        if (this.stompClient && this.stompClient.connected) {
            const payload = {
                type: type,
                sender: this.userId,
                data: data // { message: "..." } ?�태가 data ?�드 ?�어�?
            };

            this.stompClient.publish({
                destination: `/pub/signal/${this.roomId}`,
                body: JSON.stringify(payload),
            });
        } else {
            console.warn('[Socket] ?�결?��? ?�아 메시지�?보낼 ???�습?�다.');
        }
    }
}
