// WebRTC 연결, 미디어 제어 로직

import { computed, ref, onUnmounted } from 'vue';
import { RoomManager } from '@/shared/api/livekit/RoomManager';

type FocusEventType = 'FOCUS' | 'SLEEP' | 'PHONE' | 'AWAY';

export function useStudyRoom() {
    const roomManager = RoomManager.getInstance();
    const isConnected = ref(false);
    const error = ref<string | null>(null);
    const messages = ref<any[]>([]);
    const remoteTracks = ref<{ participantId: string; track: any }[]>([]);
    const focusEventType = ref<FocusEventType | null>(null);
    // null이 아니고 FOCUS가 아니면 '딴짓 중(true)'으로 판단
    const isDistracted = computed(() => focusEventType.value !== null && focusEventType.value !== 'FOCUS');

    /**
     * 방 입장 함수
     * @param roomId 방 번호
     * @param userId 사용자 ID
     * @param token LiveKit 접속 토큰 (필수)
     */

    const joinRoom = async (roomId: string, userId: string, token?: string) => {
        try {
            isConnected.value = false;
            error.value = null;
            messages.value = [];
            remoteTracks.value = [];

            // 이전 리스너 제거 (중복 방지)
            roomManager.removeAllListeners();

            // 메시지 수신 리스너 등록 (채팅, 시스템 메시지)
            roomManager.onMessage((payload) => {
                const directType = payload?.eventType || payload?.data?.eventType;
                const signalType = payload?.type;

                if (signalType === 'AI_EVENT' || signalType === 'AI_STATE' || directType) {
                    const eventType = (directType || payload?.data?.state || payload?.data?.focusState) as FocusEventType | undefined;
                    if (eventType === 'FOCUS' || eventType === 'SLEEP' || eventType === 'PHONE' || eventType === 'AWAY') {
                        focusEventType.value = eventType;
                    } else if (typeof payload?.data?.value === 'number') {
                        focusEventType.value = payload.data.value === 1 ? 'SLEEP' : 'FOCUS';
                    }
                }

                messages.value.push(payload);
            });

            // 트랙 수신 리스너 등록 (상대방 얼굴 화면)
            roomManager.onTrackSubscribed((track, participant) => {
                if (track.kind === 'video') {
                    // 디버깅용 콘솔
                    console.log(`📹 비디오 수신: ${participant.identity}`);
                    remoteTracks.value.push({
                        participantId: participant.identity,
                        track: track,
                    });
                }
            });

            // 트랙 해제 리스너 등록 (상대방 나감, 화면 해제)
            roomManager.onTrackUnsubscribed((track, participant) => {
                // 디버깅용 콘솔
                console.log(`🚫 비디오 중단: ${participant.identity}`);
                remoteTracks.value = remoteTracks.value.filter(
                    (p) => p.participantId !== participant.identity || p.track !== track
                );
            });

            // RoomManager를 통해 입장
            await roomManager.joinStudyRoom(roomId, userId, token);

            isConnected.value = true;
            console.log('✅ useStudyRoom: 입장 완료 상태로 변경');

        } catch (e: any) {
            console.error('방 입장 실패:', e);
            error.value = e.message || '방 입장에 실패했습니다.';
            isConnected.value = false;
        }
    };

    // 방 나가기
    const leaveRoom = (disconnectHeaders?: Record<string, string>) => {
        roomManager.leaveRoom(disconnectHeaders);
        isConnected.value = false;
        messages.value = [];
        remoteTracks.value = [];
    };

    // 채팅 전송
    const sendChat = (message: string) => {
        if (!message.trim()) return;
        roomManager.sendControlMessage('CHAT', { message });
    };

    // 컴포넌트가 파괴될 때 자동으로 리소스 정리 (안전장치)
    // 주의: SPA에서 페이지 이동 시에도 방을 유지해야 한다면 이 부분은 제거하거나 조건부로 처리해야 함
    // [수정] onUnmounted 제거함 -> Page 컴포넌트에서 handleLeave를 통해 제어하도록 위임
    // onUnmounted(() => {
    //     if (isConnected.value) {
    //         leaveRoom();
    //     }
    // });


    // [추가] 테스트용: 강제로 AI 상태를 변경하는 함수
    const setDebugState = (state: FocusEventType) => {
        console.log(`🛠️ [DEBUG] 상태 강제 변경: ${state}`);
        focusEventType.value = state;
    };

    return {
        joinRoom,
        leaveRoom,
        sendChat,
        isConnected,
        error,
        messages,
        remoteTracks,
        focusEventType,
        isDistracted,
        setDebugState, // [추가] 디버그용 함수 반환
    };
}
