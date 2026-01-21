import { ref, onUnmounted } from 'vue';
import { RoomManager } from '../utils/RoomManager';

export function useStudyRoom() {
    const roomManager = RoomManager.getInstance();
    const isConnected = ref(false);
    const error = ref<string | null>(null);
    const messages = ref<any[]>([]);

    const joinRoom = async (roomId: string, memberId: string) => {
        try {
            isConnected.value = false;
            error.value = null;
            error.value = null;
            messages.value = [];

            // 이전 리스너 제거 (중복 방지)
            roomManager.removeAllListeners();

            // 메시지 수신 리스너 등록
            roomManager.onMessage((payload) => {
                messages.value.push(payload);
            });

            await roomManager.joinStudyRoom(roomId, memberId);

            isConnected.value = true;
        } catch (e: any) {
            console.error('방 입장 실패:', e);
            error.value = e.message || '방 입장에 실패했습니다.';
            isConnected.value = false;
        }
    };

    const leaveRoom = () => {
        roomManager.leaveRoom();
        isConnected.value = false;
        messages.value = [];
    };

    const sendChat = (message: string) => {
        roomManager.sendControlMessage('CHAT', { message });
    };

    // 컴포넌트가 파괴될 때 자동으로 리소스 정리 (안전장치)
    // 주의: SPA에서 페이지 이동 시에도 방을 유지해야 한다면 이 부분은 제거하거나 조건부로 처리해야 함
    onUnmounted(() => {
        // leaveRoom(); 
    });

    return {
        joinRoom,
        leaveRoom,
        sendChat,
        isConnected,
        error,
        messages,
    };
}
