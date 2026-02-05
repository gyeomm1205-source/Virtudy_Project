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
    
    // 상대방 AI, 집중도, 닉네임, 아바타 상태 관리
    const remoteParticipantStates = ref<Record<string, FocusEventType>>({});
    const remoteParticipantScores = ref<Record<string, number>>({});
    const remoteParticipantNames = ref<Record<string, string>>({});
    const remoteParticipantAvatars = ref<Record<string, AvatarConfig>>({});
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

    // 시스템 메시지 추가 헬퍼
    const addSystemMessage = (text: string) => {
        messages.value.push({
            type: 'SYSTEM',
            message: text,
            timestamp: Date.now()
        });
    };

    const joinRoom = async (
        roomId: string, 
        userId: string, 
        token?: string, 
        nickName?: string,
        avatarConfig?: AvatarConfig
    ) => {
        try {
            // 초기화
            isConnected.value = false;
            error.value = null;
            messages.value = [];
            remoteTracks.value = [];
            remoteParticipantStates.value = {}; 
            remoteParticipantScores.value = {};
            // 닉네임 맵은 초기화하되, 재입장 시 꼬이지 않게 주의
            remoteParticipantNames.value = {};
            remoteParticipantAvatars.value = {};
            remoteParticipantJoinedAt.value = {};
            remoteParticipantOrder.value = [];
            
            localNickName.value = nickName || userId;
            localUserId.value = userId;
            localAvatarConfig.value = avatarConfig || null;

            // 메시지 수신 리스너 등록 (채팅, 시스템 메시지)
            roomManager.onMessage((payload, senderId) => {
                // AI 데이터(category 필드 있음)는 채팅에 추가하지 않음 (HEAD Logic)
                if (payload && payload.category) return;

                // WebRTC Broadcast 데이터 처리 (상대방 AI 상태)
                // senderId가 있고, topic이 AI_STATUS인 경우
                if (senderId && payload && payload.topic === 'AI_STATUS') {
                  // 상태 업데이트  
                  if (payload.status) remoteParticipantStates.value[senderId] = payload.status as FocusEventType;
                  // 점수 업데이트  
                  if (typeof payload.score === 'number') remoteParticipantScores.value[senderId] = payload.score;
                    return;
                }

                // 유저 정보 수신 (닉네임/아바타)
                if (senderId && payload && payload.topic === 'USER_INFO') {
                    // 닉네임이 payload.nickName 또는 payload.data.nickName에 있을 수 있음
                    const rNick = payload.nickName || payload.data?.nickName;
                    const rAvatar = payload.avatar || payload.data?.avatar;
                    
                    const isNewUser = !remoteParticipantNames.value[senderId];

                    if (rNick) remoteParticipantNames.value[senderId] = rNick;
                    if (rAvatar) remoteParticipantAvatars.value[senderId] = rAvatar;

                    // 닉네임을 처음 알았을 때 입장 메시지 출력
                    if (isNewUser && rNick) {
                        addSystemMessage(`${rNick}님이 들어왔습니다.`);
                    }
                    return;
                }

                // 정보 요청 응답
                if (payload && payload.topic === 'USER_INFO_REQUEST') {
                    if (payload.targetId && payload.targetId === localUserId.value) {
                        roomManager.sendLiveKitData('USER_INFO', { 
                            nickName: localNickName.value,
                            avatar: localAvatarConfig.value 
                        });
                    }
                    return;
                }
                
                // 방 정보 업데이트 처리
                if (payload?.type === 'ROOM_UPDATED' && payload?.data) {
                    roomInfoUpdate.value = payload.data as RoomInfoUpdate;
                    return;
                }

                // 일반 채팅 처리
                if (payload?.type === 'CHAT') {
                    messages.value.push(payload);
                    return;
                }
                
                // AI 이벤트 처리 (기존 로직 유지)
                const directType = payload?.eventType || payload?.data?.eventType;
                const signalType = payload?.type;
                if (signalType === 'AI_EVENT' || signalType === 'AI_STATE' || directType) {
                    // Only apply local AI events (LiveKit broadcasts include senderId).
                    if (senderId) {
                        return;
                    }
                    const eventType = (directType || payload?.data?.state || payload?.data?.focusState) as FocusEventType | undefined;
                    if (eventType === 'FOCUS' || eventType === 'SLEEP' || eventType === 'PHONE' || eventType === 'AWAY') {
                        focusEventType.value = eventType;
                    } else if (typeof payload?.data?.value === 'number') {
                        focusEventType.value = payload.data.value === 1 ? 'SLEEP' : 'FOCUS';
                    }
                }
            });

            // 트랙 구독
            roomManager.onTrackSubscribed((track, participant) => {
                if (track.kind === 'video') {
                    remoteTracks.value.push({ participantId: participant.identity, track: track });
                    if (!remoteParticipantNames.value[participant.identity]) {
                        roomManager.sendLiveKitData('USER_INFO_REQUEST', { targetId: participant.identity });
                    }
                }
                updateParticipantOrder(participant);
            });

            // 트랙 해제 리스너 등록 (상대방 나감, 화면 해제)
            roomManager.onTrackUnsubscribed((track, participant) => {
                remoteTracks.value = remoteTracks.value.filter(
                    (p) => p.participantId !== participant.identity || p.track !== track
                );
            });

            // 참가자 입장 (LiveKit 이벤트)
            roomManager.onParticipantConnected((_participant) => {
                updateParticipantOrder(_participant);
                roomManager.sendLiveKitData('USER_INFO', { 
                    nickName: localNickName.value,
                    avatar: localAvatarConfig.value 
                });
                // 여기서는 아직 닉네임을 모를 수 있으므로 USER_INFO 수신 시점에 메시지를 띄움
            });

            // 참가자 퇴장 (LiveKit 이벤트)
            roomManager.onParticipantDisconnected((participant) => {
                const pId = participant.identity;
                
                // 퇴장 메시지 출력 (닉네임이 있으면 닉네임, 없으면 ID)
                const leaverName = remoteParticipantNames.value[pId] || '알 수 없는 사용자';
                addSystemMessage(`${leaverName}님이 떠났습니다.`);

                // 리소스 정리
                remoteTracks.value = remoteTracks.value.filter((p) => p.participantId !== pId);
                delete remoteParticipantStates.value[pId];
                delete remoteParticipantScores.value[pId];
                // 닉네임은 삭제하지 않음 -> 채팅 로그에서 닉네임 유지됨
                // delete remoteParticipantNames.value[pId]; 
                
                delete remoteParticipantAvatars.value[pId];
                removeParticipantOrder(pId);
            });

            // RoomManager를 통해 입장
            await roomManager.joinStudyRoom(roomId, userId, token);
            isConnected.value = true;

            // 입장 직후 내 정보 방송
            roomManager.sendLiveKitData('USER_INFO', { 
                nickName: localNickName.value,
                avatar: localAvatarConfig.value 
            });
            addSystemMessage('채팅방에 입장했습니다.');

            // 이미 있는 참가자 순서 정렬
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
        // 상태 초기화
        remoteParticipantStates.value = {};
        remoteParticipantScores.value = {};
        remoteParticipantNames.value = {};
        remoteParticipantAvatars.value = {};
        remoteParticipantJoinedAt.value = {};
        remoteParticipantOrder.value = [];
    };

    const sendChat = (message: string) => {
        if (!message.trim()) return;
        // 내 닉네임을 포함해서 보낼 수도 있지만, 보통 수신 측에서 ID 매핑으로 처리
        roomManager.sendControlMessage('CHAT', { message });
    };

    const setDebugState = (state: FocusEventType) => {
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
        setDebugState,
        roomInfoUpdate,
    };
}