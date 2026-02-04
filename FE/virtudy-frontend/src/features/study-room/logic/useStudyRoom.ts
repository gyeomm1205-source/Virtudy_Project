// WebRTC 연결, 미디어 제어 로직

import { computed, ref, onUnmounted } from 'vue';
import { RoomManager } from '@/shared/api/livekit/RoomManager';
import type { AvatarConfig } from '@/shared/types/common.types';

type FocusEventType = 'FOCUS' | 'SLEEP' | 'PHONE' | 'AWAY';
type RoomInfoUpdate = {
    roomId: string;
    title?: string;
    description?: string;
};

export function useStudyRoom() {
    const roomManager = RoomManager.getInstance();
    const isConnected = ref(false);
    const error = ref<string | null>(null);
    const messages = ref<any[]>([]);
    const remoteTracks = ref<{ participantId: string; track: any }[]>([]);
    const focusEventType = ref<FocusEventType | null>(null);

    // 상대방 AI 상태 저장용 (key: participantId, value: status)
    const remoteParticipantStates = ref<Record<string, FocusEventType>>({});
    // 상대방 집중도 점수 저장용 (key: participantId, value: score)
    const remoteParticipantScores = ref<Record<string, number>>({});
    // 상대방 닉네임 저장용 (key: participantId, value: nickName)
    const remoteParticipantNames = ref<Record<string, string>>({});
    // 상대방 아바타 설정 저장용 (key: participantId, value: AvatarConfig)
    const remoteParticipantAvatars = ref<Record<string, AvatarConfig>>({});

    // remote participant join order
    const remoteParticipantJoinedAt = ref<Record<string, number>>({});
    const remoteParticipantOrder = ref<string[]>([]);

    const localNickName = ref<string | null>(null);
    const localUserId = ref<string | null>(null);

    // 내 아바타 정보를 저장해두었다가 요청 시 보냄
    const localAvatarConfig = ref<AvatarConfig | null>(null);

    // 방 정보 업데이트 이벤트
    const roomInfoUpdate = ref<RoomInfoUpdate | null>(null);

    // null이 아니고 FOCUS가 아니면 '딴짓 중(true)'으로 판단
    const isDistracted = computed(() => focusEventType.value !== null && focusEventType.value !== 'FOCUS');

    const updateParticipantOrder = (participant: { identity: string; joinedAt?: Date | undefined }) => {
        if (!participant?.identity) return;
        if (participant.identity === localUserId.value) return;
        const joinedAt = participant.joinedAt ? participant.joinedAt.getTime() : Date.now();
        if (!remoteParticipantJoinedAt.value[participant.identity]) {
            remoteParticipantJoinedAt.value[participant.identity] = joinedAt;
        }
        const ordered = Object.entries(remoteParticipantJoinedAt.value)
            .sort((a, b) => a[1] - b[1] || a[0].localeCompare(b[0]))
            .map(([id]) => id);
        remoteParticipantOrder.value = ordered;
    };

    const removeParticipantOrder = (participantId: string) => {
        if (!participantId) return;
        if (remoteParticipantJoinedAt.value[participantId]) {
            delete remoteParticipantJoinedAt.value[participantId];
        }
        remoteParticipantOrder.value = remoteParticipantOrder.value.filter((id) => id !== participantId);
    };

    /**
     * 방 입장 함수
     * @param roomId 방 번호
     * @param userId 사용자 ID
     * @param token LiveKit 접속 토큰 (필수)
     */

    const joinRoom = async (
        roomId: string, 
        userId: string, 
        token?: string, 
        nickName?: string,
        avatarConfig?: AvatarConfig
    ) => {
        try {
            isConnected.value = false;
            error.value = null;
            messages.value = [];
            remoteTracks.value = [];
            remoteParticipantStates.value = {}; // 초기화
            remoteParticipantScores.value = {};
            remoteParticipantNames.value = {};
            remoteParticipantAvatars.value = {};
            remoteParticipantJoinedAt.value = {};
            remoteParticipantOrder.value = [];

            localNickName.value = nickName || userId;
            localUserId.value = userId;
            localAvatarConfig.value = avatarConfig || null;

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

                // [추가] 상대방 닉네임 업데이트
                if (senderId && payload && payload.topic === 'USER_INFO') {
                    // 닉네임 저장
                    const rNick = payload.nickName || payload.data?.nickName;
                    if (rNick) remoteParticipantNames.value[senderId] = rNick;
                    
                    // 아바타 저장
                    const rAvatar = payload.avatar || payload.data?.avatar;
                    if (rAvatar) remoteParticipantAvatars.value[senderId] = rAvatar;
                    
                    return;
                }

                // 정보 요청이 오면 내 정보(닉네임 + 아바타) 전송
                if (payload && payload.topic === 'USER_INFO_REQUEST') {
                    if (payload.targetId && payload.targetId === localUserId.value) {
                        roomManager.sendLiveKitData('USER_INFO', { 
                            nickName: localNickName.value,
                            avatar: localAvatarConfig.value // 아바타 정보 포함해서 전송
                        });
                    }
                    return;
                }

                // 방 정보 업데이트 이벤트 처리
                if (payload?.type === 'ROOM_UPDATED' && payload?.data) {
                    roomInfoUpdate.value = payload.data as RoomInfoUpdate;
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

            // Track subscription (remote video)
            roomManager.onTrackSubscribed((track, participant) => {
                if (track.kind === 'video') {
                    console.log(`[TrackSubscribed] ${participant.identity}`);
                    remoteTracks.value.push({
                        participantId: participant.identity,
                        track: track,
                    });

                    if (!remoteParticipantNames.value[participant.identity]) {
                        roomManager.sendLiveKitData('USER_INFO_REQUEST', { targetId: participant.identity });
                    }
                }
                updateParticipantOrder(participant);
            });


            // 트랙 해제 리스너 등록 (상대방 나감, 화면 해제)
            roomManager.onTrackUnsubscribed((track, participant) => {
                // 디버깅용 콘솔
                console.log(`🚫 비디오 중단: ${participant.identity}`);
                remoteTracks.value = remoteTracks.value.filter(
                    (p) => p.participantId !== participant.identity || p.track !== track
                );
            });

            // 새 참가자가 들어오면 내 정보(닉네임 + 아바타)를 먼저 보냄
            roomManager.onParticipantConnected((_participant) => {
                updateParticipantOrder(_participant);
                roomManager.sendLiveKitData('USER_INFO', { 
                    nickName: localNickName.value,
                    avatar: localAvatarConfig.value 
                });
            });

            // 참가자 퇴장 리스너 등록 (아바타/상태 정리)
            roomManager.onParticipantDisconnected((participant) => {
                remoteTracks.value = remoteTracks.value.filter((p) => p.participantId !== participant.identity);
                delete remoteParticipantStates.value[participant.identity];
                delete remoteParticipantScores.value[participant.identity];
                delete remoteParticipantNames.value[participant.identity];
                delete remoteParticipantAvatars.value[participant.identity]; // 아바타 정리
                removeParticipantOrder(participant.identity);
            });

            // RoomManager를 통해 입장
            await roomManager.joinStudyRoom(roomId, userId, token);

            isConnected.value = true;
            console.log('✅ useStudyRoom: 입장 완료 상태로 변경');

            // 입장 직후 내 정보(닉네임 + 아바타) 방송
            roomManager.sendLiveKitData('USER_INFO', { 
                nickName: localNickName.value,
                avatar: localAvatarConfig.value 
            });

            const room = roomManager.getRoom();
            if (room?.remoteParticipants) {
                room.remoteParticipants.forEach((participant) => {
                    updateParticipantOrder(participant);
                });
            }

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
        remoteParticipantNames.value = {};
        remoteParticipantJoinedAt.value = {};
        remoteParticipantOrder.value = [];
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
        remoteParticipantScores, 
        remoteParticipantNames,
        remoteParticipantAvatars,
        remoteParticipantOrder,
        focusEventType,
        isDistracted,
        setDebugState, // [추가] 디버그용 함수 반환
        roomInfoUpdate, 
    };
}
