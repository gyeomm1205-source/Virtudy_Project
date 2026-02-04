import { Room, RoomEvent, RemoteParticipant, RemoteTrackPublication, RemoteTrack, LocalVideoTrack, Track } from 'livekit-client';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// ë°±ì—”??URL ?¤ì • (?˜ê²½ ë³€???ëŠ” ?ìˆ˜ë¡?ê´€ë¦?ê¶Œì¥)
// ë°±ì—”??URL ?¤ì • (?˜ê²½ ë³€???ëŠ” ?ìˆ˜ë¡?ê´€ë¦?ê¶Œì¥)
// const LIVEKIT_URL = import.meta.env.VITE_LIVEKIT_URL || 'ws://127.0.0.1:7880';
// const SOCKET_URL = import.meta.env.VITE_SOCKET_URL || 'http://127.0.0.1:8081/ws';

// // [ì¶”ê?] AI ?œë²„ ?¹ì†Œì¼?ì£¼ì†Œ (FastAPI ??AI ?œë²„???¹ì†Œì¼??”ë“œ?¬ì¸??
// const AI_SOCKET_URL = import.meta.env.VITE_AI_SOCKET_URL || 'ws://127.0.0.1:8000/ws/analysis';

// ë°±ì—”??URL ?¤ì • (?˜ê²½ ë³€???ëŠ” ?ìˆ˜ë¡?ê´€ë¦?ê¶Œì¥)
// const LIVEKIT_URL = import.meta.env.VITE_LIVEKIT_URL || 'ws://127.0.0.1:7880'; // ?˜ê²½ë³€???°ì„  ?¬ìš©
const LIVEKIT_URL = import.meta.env.VITE_LIVEKIT_URL || 'ws://127.0.0.1:7880';

// const SOCKET_URL = 'http://127.0.0.1:8081/ws'; // ë°±ì—”???”êµ¬?¬í•­: 8081?¬íŠ¸ë¡?ì§ì ‘ ?°ê²°
const SOCKET_URL = import.meta.env.VITE_SOCKET_URL || 'http://127.0.0.1:8081/ws';

// [ì¶”ê?] AI ?œë²„ ?¹ì†Œì¼?ì£¼ì†Œ (FastAPI ??AI ?œë²„???¹ì†Œì¼??”ë“œ?¬ì¸??
// const AI_SOCKET_URL = 'ws://127.0.0.1:8000/ws/analysis';
const AI_SOCKET_URL = import.meta.env.VITE_AI_SOCKET_URL || 'ws://127.0.0.1:8000/ws/analysis';


export class RoomManager {
    private static instance: RoomManager;
    private room: Room | null = null;
    private stompClient: Client | null = null;
    private aiSocket: WebSocket | null = null; // [ì¶”ê?] AI??ì§í†µ ?Œì¼“
    private roomId: string = '';
    private userId: string = ''; // [ì¶”ê?] userId ?€??

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

    // 1. ?µí•© ?°ê²° ?¨ìˆ˜ (?…ì¥ API -> LiveKit ?°ê²° -> ?Œì¼“ ?°ê²°)
    async joinStudyRoom(roomId: string, userId: string, token?: string) {
        this.roomId = roomId;
        this.userId = userId; // [ì¶”ê?] user ID ?€??

        try {
            // [?˜ì •] ?„ë¡ ?¸ì—”??ë¡œì»¬ ? í° ?ì„±ê¸??¬ìš© (ë°±ì—”??ë¯¸êµ¬???€??
            const { LocalTokenGenerator } = await import('@/shared/lib/LocalTokenGenerator');
            const liveKitToken = await LocalTokenGenerator.generateToken(roomId, userId);

            // ? í° ê²°ì •: ?¸ìë¡?ë°›ì? ? í°???†ìœ¼ë©?ë¡œì»¬ ?ì„± ? í° ?¬ìš©
            const finalToken = token || liveKitToken;

            const accessToken = localStorage.getItem('accessToken') || '';

            console.log(`[RoomManager] ? í° ?•ì¸ ?„ë£Œ (${token ? 'External' : 'Local'}). LiveKit ?°ê²°???œì‘?©ë‹ˆ??`);

            // 1-2. LiveKit ?°ê²° (ë¯¸ë””???Œë ˆ??
            await this.connectLiveKit(finalToken);

            // 1-3. WebSocket ?°ê²° (ì»¨íŠ¸ë¡??Œë ˆ??
            this.connectWebSocket(accessToken);
            // [ì¶”ê?]1-4. ?Œì¼“ ì§í†µ ?°ê²° (ì§‘ì¤‘???°ì´??
            this.connectAISocket();

            console.log(`[RoomManager] ë°?${roomId} ?…ì¥ ?„ë£Œ`);
        } catch (error) {
            console.error('[RoomManager] ë°??…ì¥ ?¤íŒ¨:', error);
            this.leaveRoom(); // ?¤íŒ¨ ???•ë¦¬
            throw error;
        }
    }

    // 2-1. ê°€??ì¹´ë©”???¸ë™ ?ì„± (AIê°€ ì¹´ë©”???ìœ  ???¬ìš©)
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
            throw new Error('ê°€??ì¹´ë©”???¤íŠ¸ë¦¼ì—??ë¹„ë””???¸ë™??ì°¾ì„ ???†ìŠµ?ˆë‹¤.');
        }
        return new LocalVideoTrack(videoTrack);
    }

    // 2. LiveKit ?°ê²° ë¡œì§
    private async connectLiveKit(token: string) {
        this.room = new Room({
            // ?¤ë””??ë¹„ë””???ë™ ìº¡ì²˜ ?¤ì •
            adaptiveStream: true,
            dynacast: true,
        });

        // LiveKit ?´ë²¤??ë¦¬ìŠ¤???±ë¡
        this.room.on(RoomEvent.TrackSubscribed, (track, publication, participant) => {
            this.handleTrackSubscribed(track, publication, participant);
        });

        this.room.on(RoomEvent.TrackUnsubscribed, (track, publication, participant) => {
            this.handleTrackUnsubscribed(track, publication, participant);
        });

        this.room.on(RoomEvent.Disconnected, (reason) => {
            console.warn('[LiveKit] ?°ê²°???Šì–´ì¡ŒìŠµ?ˆë‹¤. Reason:', reason);
        });

        this.room.on(RoomEvent.DataReceived, (payload, participant, _kind, _topic) => {
            const strData = new TextDecoder().decode(payload);
            try {
                const data = JSON.parse(strData);
                console.log(`[LiveKit] ?°ì´???˜ì‹  (${participant?.identity}):`, data);
                // ê¸°ì¡´ ë©”ì‹œì§€ ë¦¬ìŠ¤?ˆì—ê²??„ë‹¬ (useAiHandler ?±ì—??ì²˜ë¦¬)
                // [?˜ì •] sender ?•ë³´(participant.identity)ë¥??¨ê»˜ ?„ë‹¬
                this.messageListeners.forEach(listener => listener(data, participant?.identity));
            } catch (e) {
                console.warn('[LiveKit] ?°ì´???Œì‹± ?¤íŒ¨:', strData);
            }
        });

        try {
            await this.room.connect(LIVEKIT_URL, token);
            console.log('[LiveKit] ?œë²„ ?°ê²° ?±ê³µ');

            await this.room.localParticipant.setMicrophoneEnabled(false);

            // [ìˆ˜ì •] AI ë´‡(ì„œë²„) ë°©ì‹ì„ ì‚¬ìš©í•˜ë¯€ë¡œ ë¸Œë¼ìš°ì €ê°€ ì‹¤ì œ ì¹´ë©”ë¼ë¥¼ ì†¡ì¶œí•´ì•¼ í•¨
            // ê°€ìƒ ì¹´ë©”ë¼(Avatar)ëŠ” ë¡œì»¬ UIì—ì„œ ì²˜ë¦¬í•˜ê³ , ì†¡ì¶œì€ ì‹¤ì œ ì¹´ë©”ë¼ë¡œ í•´ì•¼ AIê°€ ë¶„ì„ ê°€ëŠ¥
            console.log('[LiveKit] ì‹¤ì œ ì¹´ë©”ë¼ë¥¼ ê³ í™”ì§ˆ(720p)ë¡œ ì‹œì‘í•©ë‹ˆë‹¤.');
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

    // 3. WebSocket(SockJS+Stomp) ?°ê²° ë¡œì§

    // private connectWebSocket() {
    private connectWebSocket(token: String) {
        this.stompClient = new Client({
            webSocketFactory: () => new SockJS(SOCKET_URL),
            connectHeaders: {
                memberId: this.userId,
                roomId: this.roomId,
                // [ì¤‘ìš”] JWT ? í° ì¶”ê? (Bearer ê³µë°± ì£¼ì˜)
                Authorization: `Bearer ${token}`,
            },
            debug: (str) => {
                // console.log(`[Stomp] ${str}`); // ?”ë²„ê¹??œì—ë§?ì¼œê¸° (ë¡œê·¸ ?ˆë¬´ ë§ìŒ)
            },
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('[Stomp] ?Œì¼“ ?°ê²° ?±ê³µ');
                this.stompClient?.subscribe(`/sub/room/${this.roomId}`, (message) => {
                    const payload = JSON.parse(message.body);
                    this.handleSocketMessage(payload);
                });
            },
            onStompError: (frame) => {
                console.error('[Stomp] ?ëŸ¬ ë°œìƒ:', frame.headers['message']);
            },
        });

        this.stompClient.activate();
    }
    // [ì¶”ê?] 1-4. AI ?œë²„ ?Œì¼“ ?°ê²° (ì§í†µ)
    private connectAISocket() {
        // AI ?œë²„??ë³´ë‚¼ ì£¼ì†Œ (?? ë°©ID?€ ? ì?IDë¥?ê²½ë¡œ???¬í•¨)
        const url = `${AI_SOCKET_URL}/${this.roomId}/${this.userId}`;

        console.log(`[AI-Socket] ?°ê²° ?œë„: ${url}`);
        this.aiSocket = new WebSocket(url);

        this.aiSocket.onopen = () => {
            console.log("?¤– [AI-Socket] ?°ê²° ?±ê³µ!");
        };

        this.aiSocket.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                // AIê°€ ë³´ë‚¸ ?°ì´?°ë? ê³µí†µ ?¸ë“¤?¬ë¡œ ?˜ê?
                const payload = {
                    type: 'AI_EVENT',
                    data: {
                        eventType: data.eventType || data.status,
                        value: data.value
                    }
                };
                this.handleSocketMessage(payload);
            } catch (error) {
                console.error("[AI-Socket] ?°ì´???Œì‹± ?¤ë¥˜:", error);
            }
        };

        this.aiSocket.onclose = () => {
            console.log("?¤– [AI-Socket] ?°ê²° ì¢…ë£Œ");
        };

        this.aiSocket.onerror = (error) => {
            console.error("?¤– [AI-Socket] ?ëŸ¬:", error);
        };
    }

    // ?¸ë™ êµ¬ë… ?¸ë“¤??(?”ë©´??ë¹„ë””???œì‹œ)
    private handleTrackSubscribed(
        track: RemoteTrack,
        _publication: RemoteTrackPublication,
        participant: RemoteParticipant
    ) {
        if (track.kind === 'video' || track.kind === 'audio') {
            console.log(`[LiveKit] ?¸ë™ ?˜ì‹ : ${participant.identity} (${track.kind})`);
            // ë¦¬ìŠ¤?ˆë“¤?ê²Œ ?Œë¦¼
            this.trackListeners.forEach(listener => listener(track, participant));
        }
    }

    // ?¸ë™ êµ¬ë… ?´ì œ ?¸ë“¤??
    private handleTrackUnsubscribed(
        track: RemoteTrack,
        _publication: RemoteTrackPublication,
        participant: RemoteParticipant
    ) {
        console.log(`[LiveKit] ?¸ë™ ?´ì œ: ${participant.identity} (${track.kind})`);
        this.trackCleanupListeners.forEach(listener => listener(track, participant));
    }

    // ë©”ì‹œì§€ ë¦¬ìŠ¤??ê´€ë¦?
    // [?˜ì •] listener ?€??ë³€ê²? payload + senderId
    private messageListeners: ((payload: any, senderId?: string) => void)[] = [];
    // ?¸ë™ ë¦¬ìŠ¤??ê´€ë¦?
    private trackListeners: ((track: RemoteTrack, participant: RemoteParticipant) => void)[] = [];
    private trackCleanupListeners: ((track: RemoteTrack, participant: RemoteParticipant) => void)[] = [];

    // ë©”ì‹œì§€ ?˜ì‹  ?´ë²¤???±ë¡
    onMessage(callback: (payload: any, senderId?: string) => void) {
        this.messageListeners.push(callback);
    }

    // ?¸ë™ ?˜ì‹  ?´ë²¤???±ë¡
    onTrackSubscribed(callback: (track: RemoteTrack, participant: RemoteParticipant) => void) {
        this.trackListeners.push(callback);
    }

    // ?¸ë™ ?´ì œ ?´ë²¤???±ë¡
    onTrackUnsubscribed(callback: (track: RemoteTrack, participant: RemoteParticipant) => void) {
        this.trackCleanupListeners.push(callback);
    }

    // ëª¨ë“  ë¦¬ìŠ¤???œê±° (ì¤‘ë³µ ë°©ì???
    removeAllListeners() {
        this.messageListeners = [];
        this.trackListeners = [];
        this.trackCleanupListeners = [];
    }

    // ?Œì¼“ ë©”ì‹œì§€ ?¸ë“¤??(?íƒœ ?…ë°?´íŠ¸)
    private handleSocketMessage(payload: any) {
        console.log('[Socket] ë©”ì‹œì§€ ?˜ì‹ :', payload);
        // ?±ë¡??ë¦¬ìŠ¤?ˆë“¤?ê²Œ ?Œë¦¼ (SYSTEM ë©”ì‹œì§€?´ë?ë¡?senderId??undefined ì²˜ë¦¬)
        this.messageListeners.forEach(listener => listener(payload, undefined));
    }

    // ë°??˜ê?ê¸?(Cleanup)
    leaveRoom(disconnectHeaders?: Record<string, string>) {
        this.room?.disconnect();
        this.room = null;
        // [ì¶”ê?] AI ?Œì¼“ ì¢…ë£Œ
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
        this.messageListeners = []; // ë¦¬ìŠ¤??ì´ˆê¸°??
        console.log('[RoomManager] ë°??´ì¥ ë°?ë¦¬ì†Œ???•ë¦¬ ?„ë£Œ');
    }

    // [ì¶”ê?] 4. LiveKit ?°ì´???„ì†¡ (Broadcast)
    // topic: ?°ì´??ì£¼ì œ (?? 'AI_STATUS')
    // data: ?„ì†¡??ê°ì²´
    async sendLiveKitData(topic: string, data: any) {
        // [Fix] Check ConnectionState.Connected explicitly
        if (!this.room || !this.room.localParticipant || this.room.state !== 'connected') { // String literal check due to enum import complexity or check LiveKit docs
            // LiveKit RoomState: 'disconnected' | 'connected' | 'reconnecting' | 'connecting'
            console.warn(`[RoomManager] LiveKit ë¯¸ì—°ê²??íƒœ(${this.room?.state})???°ì´?°ë? ë³´ë‚¼ ???†ìŠµ?ˆë‹¤.`, topic);
            return;
        }

        try {
            const strData = JSON.stringify({ topic, ...data });
            const encoder = new TextEncoder();
            const payload = encoder.encode(strData);

            await this.room.localParticipant.publishData(payload, {
                reliable: true,
            });
            console.log(`?“¡ [LiveKit-Broadcast] ${topic} ?„ì†¡ ?„ë£Œ`, data);
        } catch (e) {
            console.warn(`[LiveKit-Broadcast] ?„ì†¡ ?¤íŒ¨ (?¼ì‹œ???¤ë¥˜ ê°€?¥ì„±):`, e);
            // Error explicitly handled, no need to throw
        }
    }

    // ?œì–´ ë©”ì‹œì§€ ?„ì†¡ ?¬í¼
    sendControlMessage(type: string, data: any) {
        if (this.stompClient && this.stompClient.connected) {
            const payload = {
                type: type,
                sender: this.userId,
                data: data // { message: "..." } ?•íƒœê°€ data ?„ë“œ ?¤ì–´ê°?
            };

            this.stompClient.publish({
                destination: `/pub/signal/${this.roomId}`,
                body: JSON.stringify(payload),
            });
        } else {
            console.warn('[Socket] ?°ê²°?˜ì? ?Šì•„ ë©”ì‹œì§€ë¥?ë³´ë‚¼ ???†ìŠµ?ˆë‹¤.');
        }
    }
}
