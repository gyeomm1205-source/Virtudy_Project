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
    // [추가] 상대방 AI 상태 저장용 (key: participantId, value: status)
    const remoteParticipantStates = ref<Record<string, FocusEventType>>({});
    // [추가] 상대방 집중도 점수 저장용 (key: participantId, value: score)
    const remoteParticipantScores = ref<Record<string, number>>({});

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
            remoteParticipantStates.value = {}; // 초기화

            // 이전 리스너 제거 (중복 방지)
            // roomManager.removeAllListeners();

            // 메시지 수신 리스너 등록 (채팅, 시스템 메시지)
            roomManager.onMessage((payload, senderId) => {
                // [Fix] AI 데이터(category 필드 있음)는 채팅에 추가하지 않음 (HEAD Logic)
                if (payload && payload.category) {
                    // Local AI logic (already handled by useAiHandler via separate listener, but careful of duplication)
                    // useAiHandler registers its own listener. Here we just ignore it for chat.
                    return;
                }

                // [추가] 3. WebRTC Broadcast 데이터 처리 (상대방 AI 상태)
                // senderId가 있고, topic이 AI_STATUS인 경우
                if (senderId && payload && payload.topic === 'AI_STATUS') {
                    // 1. 상태(status) 업데이트
                    if (payload.status) {
                        console.log(`📡 [Remote-Status-Update] ${senderId}: ${payload.status}`);
                        remoteParticipantStates.value[senderId] = payload.status as FocusEventType;
                    }
                    // 2. 점수(score) 업데이트
                    if (typeof payload.score === 'number') {
                        console.log(`📡 [Remote-Score-Update] ${senderId}: ${payload.score}`);
                        remoteParticipantScores.value[senderId] = payload.score;
                    }
                    return;
                }

                // Origin/FE Logic (handling other AI formats if necessary, but ensuring chat is protected)
                const directType = payload?.eventType || payload?.data?.eventType;
                const signalType = payload?.type;

                if (signalType === 'AI_EVENT' || signalType === 'AI_STATE' || directType) {
                    const eventType = (directType || payload?.data?.state || payload?.data?.focusState) as FocusEventType | undefined;
                    if (eventType === 'FOCUS' || eventType === 'SLEEP' || eventType === 'PHONE' || eventType === 'AWAY') {
                        focusEventType.value = eventType;
                    } else if (typeof payload?.data?.value === 'number') {
                        focusEventType.value = payload.data.value === 1 ? 'SLEEP' : 'FOCUS';
                    }
                    // Do not push to chat
                    return;
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

            // 참가자 퇴장 리스너 등록 (아바타/상태 정리)
            roomManager.onParticipantDisconnected((participant) => {
                console.log(`👋 참가자 퇴장 처리: ${participant.identity}`);
                remoteTracks.value = remoteTracks.value.filter(
                    (p) => p.participantId !== participant.identity
                );
                if (remoteParticipantStates.value[participant.identity]) {
                    delete remoteParticipantStates.value[participant.identity];
                }
                if (remoteParticipantScores.value[participant.identity] !== undefined) {
                    delete remoteParticipantScores.value[participant.identity];
                }
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
        remoteParticipantStates.value = {};
        remoteParticipantScores.value = {};
    };

    // 채팅 전송
    const sendChat = (message: string) => {
        if (!message.trim()) return;
        roomManager.sendControlMessage('CHAT', { message });
    };

    // 컴포넌트가 파괴될 때 자동으로 리소스 정리 (안전장치)
    // [수정] onUnmounted 제거함 -> Page 컴포넌트에서 handleLeave를 통해 제어하도록 위임

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
        remoteParticipantStates,
        remoteParticipantScores, // [추가]
        focusEventType,
        isDistracted,
        setDebugState, // [추가] 디버그용 함수 반환
    };
}
