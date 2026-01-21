import { Room, RoomEvent, RemoteParticipant, RemoteTrackPublication, RemoteTrack } from 'livekit-client';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// 백엔드 URL 설정 (환경 변수 또는 상수로 관리 권장)
const LIVEKIT_URL = 'wss://your-livekit-server-url'; // 실제 LiveKit 서버 주소
const SOCKET_URL = 'http://localhost:8080/ws'; // 실제 백엔드 소켓 주소

export class RoomManager {
    private static instance: RoomManager;
    private room: Room | null = null;
    private stompClient: Client | null = null;
    private roomId: string = '';

    private constructor() { }

    static getInstance(): RoomManager {
        if (!RoomManager.instance) {
            RoomManager.instance = new RoomManager();
        }
        return RoomManager.instance;
    }

    // 1. 통합 연결 함수 (입장 API -> LiveKit 연결 -> 소켓 연결)
    async joinStudyRoom(roomId: string, memberId: string) {
        this.roomId = roomId;

        try {
            // 1-1. 백엔드 API 호출하여 LiveKit 토큰 가져오기
            // 실제 구현 시에는 axios 등을 사용하여 API 호출
            // const response = await api.enterRoom(roomId, memberId);
            // const liveKitToken = response.data.liveKitToken;
            const liveKitToken = 'MOCK_TOKEN_NEED_BE_IMP'; // 백엔드 구현 전 임시 토큰

            if (!liveKitToken) {
                throw new Error('LiveKit 토큰을 받아오지 못했습니다.');
            }

            // 1-2. LiveKit 연결 (미디어 플레인)
            await this.connectLiveKit(liveKitToken);

            // 1-3. WebSocket 연결 (컨트롤 플레인)
            this.connectWebSocket();

            console.log(`[RoomManager] 방 ${roomId} 입장 완료`);
        } catch (error) {
            console.error('[RoomManager] 방 입장 실패:', error);
            this.leaveRoom(); // 실패 시 정리
            throw error;
        }
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

        this.room.on(RoomEvent.Disconnected, () => {
            console.warn('[LiveKit] 연결이 끊어졌습니다.');
            // 여기서 "연결 끊김" 모달을 띄우거나 홈으로 이동
        });

        try {
            // [테스트용] 실제 서버 주소가 아니면 연결 시늉만 하고 넘어감
            if (LIVEKIT_URL.includes('your-livekit-server-url')) {
                console.warn('⚠️ [TEST MODE] 가상 연결로 진행합니다.');
                await new Promise(resolve => setTimeout(resolve, 800)); // 0.8초 대기
                return;
            }

            await this.room.connect(LIVEKIT_URL, token);
            console.log('[LiveKit] 서버 연결 성공');
        } catch (e) {
            console.error(e);
            throw e;
        }
    }

    // 3. WebSocket(SockJS+Stomp) 연결 로직
    private connectWebSocket() {
        // [테스트용] 실제 소켓 주소가 아니면 연결 시늉만 함
        if (SOCKET_URL.includes('localhost:8080')) {
            console.warn('⚠️ [TEST MODE] 가상 소켓 연결로 진행합니다.');
            return;
        }

        this.stompClient = new Client({
            // SockJS를 Factory로 주입
            webSocketFactory: () => new SockJS(SOCKET_URL),
            debug: (str) => {
                console.log(`[Stomp] ${str}`);
            },
            reconnectDelay: 5000, // 자동 재연결 시도 (선택 사항)
            onConnect: () => {
                console.log('[Stomp] 소켓 연결 성공');

                // 내 방의 제어 메시지 구독
                this.stompClient?.subscribe(`/sub/room/${this.roomId}`, (message) => {
                    const payload = JSON.parse(message.body);
                    this.handleSocketMessage(payload);
                });

                // 입장 알림 전송 (필요 시)
                // this.sendControlMessage('USER_JOIN', { ... });
            },
            onStompError: (frame) => {
                console.error('[Stomp] 에러 발생:', frame.headers['message']);
            },
        });

        this.stompClient.activate();
    }

    // 트랙 구독 핸들러 (화면에 비디오 표시)
    private handleTrackSubscribed(
        track: RemoteTrack,
        publication: RemoteTrackPublication,
        participant: RemoteParticipant
    ) {
        if (track.kind === 'video' || track.kind === 'audio') {
            // Vue 컴포넌트 등에서 사용할 수 있도록 엘리먼트 생성 또는 이벤트 발송
            // const element = track.attach();
            // document.getElementById('grid-container').appendChild(element);
            console.log(`[LiveKit] 트랙 수신: ${participant.identity} (${track.kind})`);
        }
    }

    // 메시지 리스너 관리
    private messageListeners: ((payload: any) => void)[] = [];

    // 메시지 수신 이벤트 등록
    onMessage(callback: (payload: any) => void) {
        this.messageListeners.push(callback);
    }

    // 모든 리스너 제거 (중복 방지용)
    removeAllListeners() {
        this.messageListeners = [];
    }

    // 소켓 메시지 핸들러 (상태 업데이트)
    private handleSocketMessage(payload: any) {
        console.log('[Socket] 메시지 수신:', payload);
        // 등록된 리스너들에게 알림
        this.messageListeners.forEach(listener => listener(payload));
    }

    // 방 나가기 (Cleanup)
    leaveRoom() {
        this.room?.disconnect();
        this.room = null;
        this.stompClient?.deactivate();
        this.stompClient = null;
        this.messageListeners = []; // 리스너 초기화
        console.log('[RoomManager] 방 퇴장 및 리소스 정리 완료');
    }

    // 제어 메시지 전송 헬퍼
    sendControlMessage(type: string, data: any) {
        // [테스트용] 가상 모드일 경우 에코(Loopback) 처리
        if (SOCKET_URL.includes('localhost:8080')) {
            console.log(`[Mock Send] ${type}:`, data);
            // 0.1초 뒤에 내가 보낸 메시지를 그대로 
            setTimeout(() => {
                this.handleSocketMessage({ type, ...data, sender: 'me' });
            }, 100);
            return;
        }

        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.publish({
                destination: `/pub/signal/${this.roomId}`,
                body: JSON.stringify({ type, ...data }),
            });
        } else {
            console.warn('[Socket] 연결되지 않아 메시지를 보낼 수 없습니다.');
        }
    }
}
