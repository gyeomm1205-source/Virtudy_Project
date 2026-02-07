import { Room, RoomEvent, RemoteParticipant, RemoteTrackPublication, RemoteTrack, LocalVideoTrack, Track } from 'livekit-client';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// 백엔드 URL 설정 (환경 변수 또는 상수로 관리 권장)
const LIVEKIT_URL = import.meta.env.VITE_LIVEKIT_URL || 'ws://127.0.0.1:7880';
const SOCKET_URL = import.meta.env.VITE_SOCKET_URL || 'http://127.0.0.1:8081/ws';

// [추가] AI 서버 소켓 주소 (FastAPI 등 AI 서버의 웹소켓 엔드포인트)
const AI_SERVER_ENABLED = import.meta.env.VITE_AI_SERVER_ENABLED === 'true';
const AI_SOCKET_URL = import.meta.env.VITE_AI_SOCKET_URL || 'ws://127.0.0.1:8000/ws/analysis';

// 3. [AI 서버] API 주소 (HTTP 요청용)
const AI_API_URL = import.meta.env.VITE_AI_API_URL;

export class RoomManager {
    private static instance: RoomManager;
    private room: Room | null = null;
    private stompClient: Client | null = null;
    private aiSocket: WebSocket | null = null; // [추가] AI 직통 소켓
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
            // [수정] 프론트엔드 로컬 토큰 생성기 사용 (백엔드 미구현 시 대비)
            const { LocalTokenGenerator } = await import('@/shared/lib/LocalTokenGenerator');
            const liveKitToken = await LocalTokenGenerator.generateToken(roomId, userId);

            // 토큰 결정: 인자로 받은 토큰이 없으면 로컬 생성 토큰 사용
            const finalToken = token || liveKitToken;
            const accessToken = localStorage.getItem('accessToken') || '';

            console.log(`[RoomManager] 토큰 확인 완료 (${token ? 'External' : 'Local'}). LiveKit 연결을 시작합니다.`);

            // 1-2. LiveKit 연결 (미디어 레이어)
            await this.connectLiveKit(finalToken);

            // 1-3. WebSocket 연결 (컨트롤 레이어)
            this.connectWebSocket(accessToken);
            
            if (AI_SERVER_ENABLED) {
                this.connectAISocket();
                await this.requestBotJoin(finalToken);
            }

            console.log(`[RoomManager] 방 ${roomId} 입장 완료`);
            if (AI_SERVER_ENABLED) {
                console.log(`[RoomManager] 방 ${roomId} 입장 및 AI 봇 호출 완료`);
            }
        } catch (error) {
            console.error('[RoomManager] 입장 실패:', error);
            this.leaveRoom(); // 실패 시 정리
            throw error;
        }
    }

    // 2-1. 가상 카메라 트랙 생성 (AI가 카메라 공유 시 사용)
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

        this.room.on(RoomEvent.ParticipantConnected, (participant) => {
            this.handleParticipantConnected(participant);
        });

        this.room.on(RoomEvent.ParticipantDisconnected, (participant) => {
            this.handleParticipantDisconnected(participant);
        });

        this.room.on(RoomEvent.Disconnected, (reason) => {
            console.warn('[LiveKit] 연결이 끊어졌습니다. 사유:', reason);
        });

        this.room.on(RoomEvent.DataReceived, (payload, participant, _kind, _topic) => {
            const strData = new TextDecoder().decode(payload);
            try {
                const data = JSON.parse(strData);
                console.log(`[LiveKit] 데이터 수신 (${participant?.identity}):`, data);
                
                // AI 봇 브로드캐스트 필터링 (본인 데이터만 처리)
                if (data && data.category) {
                    const targetId = data.participantId;
                    if (!targetId || targetId !== this.userId) {
                        return;
                    }
                }

                // [수정] sender 정보(participant.identity)와 함께 전달
                this.messageListeners.forEach(listener => listener(data, participant?.identity));
            } catch (e) {
                console.warn('[LiveKit] 데이터 파싱 실패:', strData);
            }
        });

        try {
            await this.room.connect(LIVEKIT_URL, token);
            console.log('[LiveKit] 서버 연결 성공');

            await this.room.localParticipant.setMicrophoneEnabled(false);

            console.log('[LiveKit] 실제 카메라를 고화질(720p)로 시작합니다.');
            await this.room.localParticipant.setCameraEnabled(true, {
                resolution: {
                    width: 1280,
                    height: 720,
                    frameRate: 30,
                }
            });

        } catch (e) {
            console.error(e);
            throw e;
        }
    }

    // [추가] AI 봇 소환 메서드 (POST 요청)
    private async requestBotJoin(token: string) {
        try {
            console.log("[Bot] AI 봇 투입 요청 중...");
            
            const response = await fetch(`${AI_API_URL}/bot/join`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    url: LIVEKIT_URL,
                    token: token,
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
            console.error("[Bot] 요청 중 에러 발생:", error);
        }
    }

    // 3. WebSocket(SockJS+Stomp) 연결 로직
    private connectWebSocket(token: string) {
        this.stompClient = new Client({
            webSocketFactory: () => new SockJS(SOCKET_URL),
            connectHeaders: {
                memberId: this.userId,
                roomId: this.roomId,
                Authorization: `Bearer ${token}`,
            },
            debug: (str) => {
                // console.log(`[Stomp] ${str}`);
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

    // 1-4. AI 서버 소켓 연결 (직통)
    private connectAISocket() {
        const url = `${AI_SOCKET_URL}/${this.roomId}/${this.userId}`;

        console.log(`[AI-Socket] 연결 시도: ${url}`);
        this.aiSocket = new WebSocket(url);

        this.aiSocket.onopen = () => {
            console.log("🚀 [AI-Socket] 연결 성공!");
        };

        this.aiSocket.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
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
            console.log("❌ [AI-Socket] 연결 종료");
        };

        this.aiSocket.onerror = (error) => {
            console.error("❗ [AI-Socket] 에러:", error);
        };
    }

    // 트랙 구독 핸들러 (화면/오디오 표시)
    private handleTrackSubscribed(
        track: RemoteTrack,
        _publication: RemoteTrackPublication,
        participant: RemoteParticipant
    ) {
        if (track.kind === 'video' || track.kind === 'audio') {
            console.log(`[LiveKit] 트랙 수신: ${participant.identity} (${track.kind})`);
            this.trackListeners.forEach(listener => listener(track, participant));
        }
    }

    private handleTrackUnsubscribed(
        track: RemoteTrack,
        _publication: RemoteTrackPublication,
        participant: RemoteParticipant
    ) {
        console.log(`[LiveKit] 트랙 해제: ${participant.identity} (${track.kind})`);
        this.trackCleanupListeners.forEach(listener => listener(track, participant));
    }

    private handleParticipantConnected(participant: RemoteParticipant) {
        console.log(`[LiveKit] 참가자 입장: ${participant.identity}`);
        this.participantConnectedListeners.forEach(listener => listener(participant));
    }

    private handleParticipantDisconnected(participant: RemoteParticipant) {
        console.log(`[LiveKit] 참가자 퇴장: ${participant.identity}`);
        this.participantDisconnectedListeners.forEach(listener => listener(participant));
    }

    private messageListeners: ((payload: any, senderId?: string) => void)[] = [];
    private trackListeners: ((track: RemoteTrack, participant: RemoteParticipant) => void)[] = [];
    private trackCleanupListeners: ((track: RemoteTrack, participant: RemoteParticipant) => void)[] = [];
    private participantConnectedListeners: ((participant: RemoteParticipant) => void)[] = [];
    private participantDisconnectedListeners: ((participant: RemoteParticipant) => void)[] = [];

    onMessage(callback: (payload: any, senderId?: string) => void) {
        this.messageListeners.push(callback);
    }

    emitLocalMessage(payload: any) {
        this.messageListeners.forEach(listener => listener(payload, undefined));
    }

    onTrackSubscribed(callback: (track: RemoteTrack, participant: RemoteParticipant) => void) {
        this.trackListeners.push(callback);
    }

    onTrackUnsubscribed(callback: (track: RemoteTrack, participant: RemoteParticipant) => void) {
        this.trackCleanupListeners.push(callback);
    }

    onParticipantConnected(callback: (participant: RemoteParticipant) => void) {
        this.participantConnectedListeners.push(callback);
    }

    onParticipantDisconnected(callback: (participant: RemoteParticipant) => void) {
        this.participantDisconnectedListeners.push(callback);
    }

    removeAllListeners() {
        this.messageListeners = [];
        this.trackListeners = [];
        this.trackCleanupListeners = [];
        this.participantConnectedListeners = [];
        this.participantDisconnectedListeners = [];
    }

    private handleSocketMessage(payload: any) {
        console.log('[Socket] 메시지 수신:', payload);
        this.messageListeners.forEach(listener => listener(payload, undefined));
    }

    leaveRoom(disconnectHeaders?: Record<string, string>) {
        this.room?.disconnect();
        this.room = null;
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
        this.messageListeners = []; 
        console.log('[RoomManager] 퇴장 및 리소스 정리 완료');
    }

    async sendLiveKitData(topic: string, data: any) {
        if (!this.room || !this.room.localParticipant || this.room.state !== 'connected') {
            console.warn(`[RoomManager] LiveKit 미연결 상태(${this.room?.state})로 데이터를 보낼 수 없습니다.`, topic);
            return;
        }

        try {
            const strData = JSON.stringify({ topic, ...data });
            const encoder = new TextEncoder();
            const payload = encoder.encode(strData);

            await this.room.localParticipant.publishData(payload, {
                reliable: true,
            });
            console.log(`✅ [LiveKit-Broadcast] ${topic} 전송 완료`, data);
        } catch (e) {
            console.warn(`[LiveKit-Broadcast] 전송 실패 (일시적 오류 가능성):`, e);
        }
    }

    sendControlMessage(type: string, data: any) {
        if (this.stompClient && this.stompClient.connected) {
            const payload = {
                type: type,
                sender: this.userId,
                data: data 
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