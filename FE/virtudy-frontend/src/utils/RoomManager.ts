import { Room, RoomEvent, RemoteParticipant, RemoteTrackPublication, RemoteTrack } from 'livekit-client';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// 백엔드 URL 설정 (환경 변수 또는 상수로 관리 권장)
const LIVEKIT_URL = 'ws://127.0.0.1:7880'; // 실제 LiveKit 서버 주소 (로컬 기본값)
const SOCKET_URL = 'http://127.0.0.1:8081/ws'; // 백엔드 요구사항: 8081포트로 직접 연결

export class RoomManager {
    private static instance: RoomManager;
    private room: Room | null = null;
    private stompClient: Client | null = null;
    private roomId: string = '';
    private memberId: string = ''; // [추가] 멤버 ID 저장

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
    async joinStudyRoom(roomId: string, memberId: string) {
        this.roomId = roomId;
        this.memberId = memberId; // [추가] 멤버 ID 저장

        try {
            // [수정] 프론트엔드 로컬 토큰 생성기 사용 (백엔드 미구현 대응)
            const { LocalTokenGenerator } = await import('./LocalTokenGenerator');
            const liveKitToken = await LocalTokenGenerator.generateToken(roomId, memberId);

            if (!liveKitToken) {
                throw new Error('LiveKit 토큰을 생성하지 못했습니다.');
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

        this.room.on(RoomEvent.TrackUnsubscribed, (track, publication, participant) => {
            this.handleTrackUnsubscribed(track, publication, participant);
        });

        this.room.on(RoomEvent.Disconnected, () => {
            console.warn('[LiveKit] 연결이 끊어졌습니다.');
            // 여기서 "연결 끊김" 모달을 띄우거나 홈으로 이동
        });

        try {
            // [수정] Test Mode 제거 및 Video: true 설정
            await this.room.connect(LIVEKIT_URL, token);
            console.log('[LiveKit] 서버 연결 성공');

            // 카메라 명시적 켜기 (connect 옵션 대신 표준 메소드 사용)
            await this.room.localParticipant.setMicrophoneEnabled(false);
            await this.room.localParticipant.setCameraEnabled(true);
        } catch (e) {
            console.error(e);
            throw e;
        }
    }

    // 3. WebSocket(SockJS+Stomp) 연결 로직
    private connectWebSocket() {
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
            console.log(`[LiveKit] 트랙 수신: ${participant.identity} (${track.kind})`);
            // 리스너들에게 알림
            this.trackListeners.forEach(listener => listener(track, participant));
        }
    }

    // 트랙 구독 해제 핸들러
    private handleTrackUnsubscribed(
        track: RemoteTrack,
        publication: RemoteTrackPublication,
        participant: RemoteParticipant
    ) {
        console.log(`[LiveKit] 트랙 해제: ${participant.identity} (${track.kind})`);
        this.trackCleanupListeners.forEach(listener => listener(track, participant));
    }

    // 메시지 리스너 관리
    private messageListeners: ((payload: any) => void)[] = [];
    // 트랙 리스너 관리
    private trackListeners: ((track: RemoteTrack, participant: RemoteParticipant) => void)[] = [];
    private trackCleanupListeners: ((track: RemoteTrack, participant: RemoteParticipant) => void)[] = [];

    // 메시지 수신 이벤트 등록
    onMessage(callback: (payload: any) => void) {
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

    // 모든 리스너 제거 (중복 방지용)
    removeAllListeners() {
        this.messageListeners = [];
        this.trackListeners = [];
        this.trackCleanupListeners = [];
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
        if (this.stompClient && this.stompClient.connected) {
            // [수정] 백엔드 SignalMessage 구조(type, sender, receiver, data)에 맞게 전송
            const payload = {
                type: type,
                sender: this.memberId,
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
