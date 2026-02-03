import { Room, RoomEvent, RemoteParticipant, RemoteTrackPublication, RemoteTrack, LocalVideoTrack, Track } from 'livekit-client';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// 백엔드 URL 설정 (환경 변수 또는 상수로 관리 권장)
// 백엔드 URL 설정 (환경 변수 또는 상수로 관리 권장)
// const LIVEKIT_URL = import.meta.env.VITE_LIVEKIT_URL || 'ws://127.0.0.1:7880';
// const SOCKET_URL = import.meta.env.VITE_SOCKET_URL || 'http://127.0.0.1:8081/ws';

// // [추가] AI 서버 웹소켓 주소 (FastAPI 등 AI 서버의 웹소켓 엔드포인트)
// const AI_SOCKET_URL = import.meta.env.VITE_AI_SOCKET_URL || 'ws://127.0.0.1:8000/ws/analysis';

// 백엔드 URL 설정 (환경 변수 또는 상수로 관리 권장)
// const LIVEKIT_URL = import.meta.env.VITE_LIVEKIT_URL || 'ws://127.0.0.1:7880'; // 환경변수 우선 사용
const LIVEKIT_URL = 'wss://i14a703.p.ssafy.io'; // 환경변수 우선 사용

// const SOCKET_URL = 'http://127.0.0.1:8081/ws'; // 백엔드 요구사항: 8081포트로 직접 연결
const SOCKET_URL = 'https://i14a703.p.ssafy.io/ws'; // 백엔드 요구사항: 8081포트로 직접 연결

// [추가] AI 서버 웹소켓 주소 (FastAPI 등 AI 서버의 웹소켓 엔드포인트)
// const AI_SOCKET_URL = 'ws://127.0.0.1:8000/ws/analysis';
const AI_SOCKET_URL = 'wss://i14a703.p.ssafy.io/fastapi/ws/analysis';


export class RoomManager {
    private static instance: RoomManager;
    private room: Room | null = null;
    private stompClient: Client | null = null;
    private aiSocket: WebSocket | null = null; // [추가] AI용 직통 소켓
    private roomId: string = '';
    private userId: string = ''; // [추가] userId 저장

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

    // 1. 통합 연결 함수 (입장 API -> LiveKit 연결 -> 소켓 연결)
    async joinStudyRoom(roomId: string, userId: string, token?: string) {
        this.roomId = roomId;
        this.userId = userId; // [추가] user ID 저장

        try {
            // [수정] 프론트엔드 로컬 토큰 생성기 사용 (백엔드 미구현 대응)
            const { LocalTokenGenerator } = await import('@/shared/lib/LocalTokenGenerator');
            const liveKitToken = await LocalTokenGenerator.generateToken(roomId, userId);

            // 토큰 결정: 인자로 받은 토큰이 없으면 로컬 생성 토큰 사용
            const finalToken = token || liveKitToken;

            const accessToken = localStorage.getItem('accessToken') || '';

            console.log(`[RoomManager] 토큰 확인 완료 (${token ? 'External' : 'Local'}). LiveKit 연결을 시작합니다.`);

            // 1-2. LiveKit 연결 (미디어 플레인)
            await this.connectLiveKit(finalToken);

            // 1-3. WebSocket 연결 (컨트롤 플레인)
            this.connectWebSocket(accessToken);
            // [추가]1-4. 소켓 직통 연결 (집중도 데이터)
            this.connectAISocket();

            console.log(`[RoomManager] 방 ${roomId} 입장 완료`);
        } catch (error) {
            console.error('[RoomManager] 방 입장 실패:', error);
            this.leaveRoom(); // 실패 시 정리
            throw error;
        }
    }

    // 2-1. 가상 카메라 트랙 생성 (AI가 카메라 점유 시 사용)
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
            throw new Error('가상 카메라 스트림에서 비디오 트랙을 찾을 수 없습니다.');
        }
        return new LocalVideoTrack(videoTrack);
    }

    // 2. LiveKit 연결 로직
    private async connectLiveKit(token: string) {
        this.room = new Room({
            // 오디오/비디오 자동 캡처 설정
            adaptiveStream: true,
            dynacast: true,
        });

        // LiveKit 이벤트 리스너 등록
        this.room.on(RoomEvent.TrackSubscribed, (track, publication, participant) => {
            this.handleTrackSubscribed(track, publication, participant);
        });

        this.room.on(RoomEvent.TrackUnsubscribed, (track, publication, participant) => {
            this.handleTrackUnsubscribed(track, publication, participant);
        });

        this.room.on(RoomEvent.ParticipantDisconnected, (participant) => {
            this.handleParticipantDisconnected(participant);
        });

        this.room.on(RoomEvent.Disconnected, (reason) => {
            console.warn('[LiveKit] 연결이 끊어졌습니다. Reason:', reason);
        });

        this.room.on(RoomEvent.DataReceived, (payload, participant, _kind, _topic) => {
            const strData = new TextDecoder().decode(payload);
            try {
                const data = JSON.parse(strData);
                console.log(`[LiveKit] 데이터 수신 (${participant?.identity}):`, data);
                // 기존 메시지 리스너에게 전달 (useAiHandler 등에서 처리)
                // [수정] sender 정보(participant.identity)를 함께 전달
                this.messageListeners.forEach(listener => listener(data, participant?.identity));
            } catch (e) {
                console.warn('[LiveKit] 데이터 파싱 실패:', strData);
            }
        });

        try {
            await this.room.connect(LIVEKIT_URL, token);
            console.log('[LiveKit] 서버 연결 성공');

            await this.room.localParticipant.setMicrophoneEnabled(false);

            // [수정] AI 봇(서버) 방식을 사용하므로 브라우저가 실제 카메라를 송출해야 함
            // 가상 카메라(Avatar)는 로컬 UI에서 처리하고, 송출은 실제 카메라로 해야 AI가 분석 가능
            console.log('[LiveKit] 실제 카메라를 시작합니다.');
            await this.room.localParticipant.setCameraEnabled(true);

            // Legacy(origin/fe) Logic:
            // await this.room.localParticipant.publishTrack(virtualTrack, { source: Track.Source.Camera });

        } catch (e) {
            console.error(e);
            throw e;
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
                // [중요] JWT 토큰 추가 (Bearer 공백 주의)
                Authorization: `Bearer ${token}`,
            },
            debug: (str) => {
                // console.log(`[Stomp] ${str}`); // 디버깅 시에만 켜기 (로그 너무 많음)
            },
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('[Stomp] 소켓 연결 성공');
                this.stompClient?.subscribe(`/sub/room/${this.roomId}`, (message) => {
                    const payload = JSON.parse(message.body);
                    this.handleSocketMessage(payload);
                });
            },
            onStompError: (frame) => {
                console.error('[Stomp] 에러 발생:', frame.headers['message']);
            },
        });

        this.stompClient.activate();
    }
    // [추가] 1-4. AI 서버 소켓 연결 (직통)
    private connectAISocket() {
        // AI 서버에 보낼 주소 (예: 방ID와 유저ID를 경로에 포함)
        const url = `${AI_SOCKET_URL}/${this.roomId}/${this.userId}`;

        console.log(`[AI-Socket] 연결 시도: ${url}`);
        this.aiSocket = new WebSocket(url);

        this.aiSocket.onopen = () => {
            console.log("🤖 [AI-Socket] 연결 성공!");
        };

        this.aiSocket.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                // AI가 보낸 데이터를 공통 핸들러로 넘김
                const payload = {
                    type: 'AI_EVENT',
                    data: {
                        eventType: data.eventType || data.status,
                        value: data.value
                    }
                };
                this.handleSocketMessage(payload);
            } catch (error) {
                console.error("[AI-Socket] 데이터 파싱 오류:", error);
            }
        };

        this.aiSocket.onclose = () => {
            console.log("🤖 [AI-Socket] 연결 종료");
        };

        this.aiSocket.onerror = (error) => {
            console.error("🤖 [AI-Socket] 에러:", error);
        };
    }

    // 트랙 구독 핸들러 (화면에 비디오 표시)
    private handleTrackSubscribed(
        track: RemoteTrack,
        _publication: RemoteTrackPublication,
        participant: RemoteParticipant
    ) {
        if (track.kind === 'video' || track.kind === 'audio') {
            console.log(`[LiveKit] 트랙 수신: ${participant.identity} (${track.kind})`);
            // 리스너들에게 알림
            this.trackListeners.forEach(listener => listener(track, participant));
        }
    }

    // 트랙 구독 해제 핸들러
    private handleTrackUnsubscribed(
        track: RemoteTrack,
        _publication: RemoteTrackPublication,
        participant: RemoteParticipant
    ) {
        console.log(`[LiveKit] 트랙 해제: ${participant.identity} (${track.kind})`);
        this.trackCleanupListeners.forEach(listener => listener(track, participant));
    }

    // 참가자 퇴장 핸들러
    private handleParticipantDisconnected(participant: RemoteParticipant) {
        console.log(`[LiveKit] 참가자 퇴장: ${participant.identity}`);
        this.participantDisconnectedListeners.forEach(listener => listener(participant));
    }

    // 메시지 리스너 관리
    // [수정] listener 타입 변경: payload + senderId
    private messageListeners: ((payload: any, senderId?: string) => void)[] = [];
    // 트랙 리스너 관리
    private trackListeners: ((track: RemoteTrack, participant: RemoteParticipant) => void)[] = [];
    private trackCleanupListeners: ((track: RemoteTrack, participant: RemoteParticipant) => void)[] = [];
    // 참가자 퇴장 리스너 관리
    private participantDisconnectedListeners: ((participant: RemoteParticipant) => void)[] = [];

    // 메시지 수신 이벤트 등록
    onMessage(callback: (payload: any, senderId?: string) => void) {
        this.messageListeners.push(callback);
    }

    // 트랙 수신 이벤트 등록
    onTrackSubscribed(callback: (track: RemoteTrack, participant: RemoteParticipant) => void) {
        this.trackListeners.push(callback);
    }

    // 트랙 해제 이벤트 등록
    onTrackUnsubscribed(callback: (track: RemoteTrack, participant: RemoteParticipant) => void) {
        this.trackCleanupListeners.push(callback);
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
        this.participantDisconnectedListeners = [];
    }

    // 소켓 메시지 핸들러 (상태 업데이트)
    private handleSocketMessage(payload: any) {
        console.log('[Socket] 메시지 수신:', payload);
        // 등록된 리스너들에게 알림 (SYSTEM 메시지이므로 senderId는 undefined 처리)
        this.messageListeners.forEach(listener => listener(payload, undefined));
    }

    // 방 나가기 (Cleanup)
    leaveRoom(disconnectHeaders?: Record<string, string>) {
        this.room?.disconnect();
        this.room = null;
        // [추가] AI 소켓 종료
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
        this.messageListeners = []; // 리스너 초기화
        console.log('[RoomManager] 방 퇴장 및 리소스 정리 완료');
    }

    // [추가] 4. LiveKit 데이터 전송 (Broadcast)
    // topic: 데이터 주제 (예: 'AI_STATUS')
    // data: 전송할 객체
    async sendLiveKitData(topic: string, data: any) {
        // [Fix] Check ConnectionState.Connected explicitly
        if (!this.room || !this.room.localParticipant || this.room.state !== 'connected') { // String literal check due to enum import complexity or check LiveKit docs
            // LiveKit RoomState: 'disconnected' | 'connected' | 'reconnecting' | 'connecting'
            console.warn(`[RoomManager] LiveKit 미연결 상태(${this.room?.state})라 데이터를 보낼 수 없습니다.`, topic);
            return;
        }

        try {
            const strData = JSON.stringify({ topic, ...data });
            const encoder = new TextEncoder();
            const payload = encoder.encode(strData);

            await this.room.localParticipant.publishData(payload, {
                reliable: true,
            });
            console.log(`📡 [LiveKit-Broadcast] ${topic} 전송 완료`, data);
        } catch (e) {
            console.warn(`[LiveKit-Broadcast] 전송 실패 (일시적 오류 가능성):`, e);
            // Error explicitly handled, no need to throw
        }
    }

    // 제어 메시지 전송 헬퍼
    sendControlMessage(type: string, data: any) {
        if (this.stompClient && this.stompClient.connected) {
            const payload = {
                type: type,
                sender: this.userId,
                data: data // { message: "..." } 형태가 data 필드 들어감
            };

            this.stompClient.publish({
                destination: `/pub/signal/${this.roomId}`,
                body: JSON.stringify(payload),
            });
        } else {
            console.warn('[Socket] 연결되지 않아 메시지를 보낼 수 없습니다.');
        }
    }
}
