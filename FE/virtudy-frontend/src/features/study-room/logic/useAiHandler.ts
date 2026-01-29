import { onMounted } from 'vue';
import { RoomManager } from '@/shared/api/livekit/RoomManager';
import { useStudyRoomAiStore } from '@/features/study-room/logic/useAiStore';
import type { AiDataPayload, AiFocusStatus, AiMouthState } from '@/features/study-room/types/ai.types';

export function useAiHandler() {
    const roomManager = RoomManager.getInstance();
    const aiStore = useStudyRoomAiStore();

    // AI 데이터 수신 핸들러
    const handleMessage = (payload: any) => {
        // payload가 AI 데이터 형식인지 확인
        // 예: { category: 'STATUS', value: 'SLEEP' }
        if (!payload || !payload.category) return;

        // 타입 안전성을 위해 캐스팅
        const aiData = payload as AiDataPayload;
        console.log('🤖 [useAiHandler] Received:', aiData); // [DEBUG]

        switch (aiData.category) {
            case 'SCORE':
                if (typeof aiData.value === 'number') {
                    aiStore.setConcentrationScore(aiData.value);
                }
                break;

            case 'STATUS':
                if (typeof aiData.value === 'string') {
                    // 유효한 상태인지 확인 후 업데이트
                    const validStatuses: AiFocusStatus[] = ['FOCUS', 'SLEEP', 'PHONE', 'AWAY'];
                    if (validStatuses.includes(aiData.value as AiFocusStatus)) {
                        aiStore.setFocusStatus(aiData.value as AiFocusStatus);
                    }
                }
                break;

            // [Modified] MOUTH, BLINK Removed for simplification as per user request
            // case 'MOUTH': ...
            // case 'BLINK': ...
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
