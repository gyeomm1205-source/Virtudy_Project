import { onMounted } from 'vue';
import { RoomManager } from '@/shared/api/livekit/RoomManager';
import { useStudyRoomAiStore } from '@/features/study-room/logic/useAiStore';
import type { AiDataPayload, AiFocusStatus, AiMouthState } from '@/features/study-room/types/ai.types';

export function useAiHandler() {
    const roomManager = RoomManager.getInstance();
    const aiStore = useStudyRoomAiStore();

    // AI 데이터 수신 핸들러
    const handleMessage = (payload: any) => {
        // [수정] RoomManager에서 보낸 포장지(AI_EVENT) 확인
        // 모양: { type: 'AI_EVENT', data: { eventType: 'FOCUS', value: 0 } }

        let aiData: AiDataPayload;

        if (payload && payload.type === 'AI_EVENT' && payload.data) {
            // 1. 포장된 경우 (RoomManager 경유)
            const innerData = payload.data;
            // FE TypeDefinition(AiDataPayload)과 맞추기 위해 변환
            // 받는 키: eventType, 보내는 코드의 키: category (Legacy)
            // 여기서 매핑해준다.
            aiData = {
                category: innerData.eventType, // eventType ("FOCUS") -> category로 매핑
                value: innerData.value
            };
        } else if (payload && payload.category) {
            // 2. 혹시라도 포장 안 된 원본이 올 경우 (방어 코드)
            aiData = payload as AiDataPayload;
        } else {
            return;
        }

        console.log('🤖 [useAiHandler] Parsed:', aiData); // [DEBUG]

        switch (aiData.category) { // 이제 매핑된 category 사용
            case 'SCORE':
                if (typeof aiData.value === 'number') {
                    aiStore.setConcentrationScore(aiData.value);
                }
                break;

            case 'FOCUS':
            case 'SLEEP':
            case 'PHONE':
            case 'AWAY':
                // eventType 자체가 상태값이므로 그대로 사용
                // (value는 0 or 1이지만, 상태 변경은 eventType 문자열로 수행)
                aiStore.setFocusStatus(aiData.category as AiFocusStatus);
                break;

            // Legacy STATUS type handling (Just in case)
            case 'STATUS':
                if (typeof aiData.value === 'string') {
                    const validStatuses: AiFocusStatus[] = ['FOCUS', 'SLEEP', 'PHONE', 'AWAY'];
                    if (validStatuses.includes(aiData.value as AiFocusStatus)) {
                        aiStore.setFocusStatus(aiData.value as AiFocusStatus);
                    }
                }
                break;
        }
    };

    onMounted(() => {
        // RoomManager의 메시지 리스너에 핸들러 등록
        // 주의: RoomManager.onMessage는 리스너를 배열에 추가하는 방식이므로
        // 중복 등록을 방지하거나, 컴포넌트 생명주기에 맞춰 관리해야 함.
        // 현재 RoomManager 구조상 개별 리스너 제거(off) 기능이 없으므로
        // useStudyRoom 등 상위에서 removeAllListeners()를 호출하는 방식에 의존하거나,
        // 여기서 등록한 리스너가 계속 남아있을 수 있음을 인지해야 함.
        // *개선점*: RoomManager에 removeMessageListener 기능이 있으면 더 좋음.

        // 임시: 그냥 등록. (상위 useStudyRoom에서 입장 시 removeAllListeners 하므로 어느 정도 안전)
        roomManager.onMessage(handleMessage);


    });

    // onUnmounted에서 리스너를 제거할 방법이 RoomManager에 없어서 생략.
    // RoomManager Singleton 특성상 페이지 이동 시 초기화 로직이 중요함.

    return {
        aiStore
    };
}
