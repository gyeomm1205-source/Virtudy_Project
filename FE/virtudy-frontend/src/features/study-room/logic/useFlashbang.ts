// 섬광탄 투척 로직 훅

import { ref, computed, onMounted, type Ref } from 'vue';
import { RoomManager } from '@/shared/api/livekit/RoomManager';
import { useAuthStore } from '@/stores/authStore';

export function useFlashbang(
    remoteParticipantScores: Ref<Record<string, number>>,
    remoteParticipantNames: Ref<Record<string, string>>
) {
    const roomManager = RoomManager.getInstance();
    const authStore = useAuthStore();
    
    // 상태 관리
    const isStunned = ref(false); // 내가 맞았는지
    const showSentFeedback = ref(false); // 내가 쐈는지 (알림용)
    const isModalOpen = ref(false); // 모달 열림 여부

    // 점수 객체(Record)를 순회하여 타겟(60점 미만) 추출
    const drowsyParticipants = computed(() => {
        const targets: { id: string; name: string; score: number }[] = [];
        
        // remoteParticipantScores.value는 { "user123": 45, "user456": 80 } 형태
        Object.entries(remoteParticipantScores.value).forEach(([userId, score]) => {
            if (score < 60) {
                targets.push({
                    id: userId,
                    name: remoteParticipantNames.value[userId] || userId, // 이름 없으면 ID 사용
                    score: score
                });
            }
        });
        
        return targets;
    });

    // 버튼 활성화 여부 (타겟이 1명이라도 있으면 true)
    const isWakeUpAvailable = computed(() => drowsyParticipants.value.length > 0);

    // 모달 제어
    const openModal = () => {
        if (isWakeUpAvailable.value) isModalOpen.value = true;
    };
    const closeModal = () => isModalOpen.value = false;

    // ⚡ 섬광탄 발사
    const sendFlashbang = (targetUserId: string) => {
        if (!authStore.userId) return;

        // 서버로 전송 (토픽: FLASHBANG)
        roomManager.sendLiveKitData('FLASHBANG', {
            targetUserId: targetUserId,
            senderId: authStore.userId
        });

        // 피드백 표시
        showSentFeedback.value = true;
        setTimeout(() => { showSentFeedback.value = false; }, 2000);
        closeModal();
    };

    // 😵 섬광탄 피격 효과 발동
    const triggerStunEffect = async () => {
        console.log("😵 으악! 섬광탄 맞음!");

        // 화면 효과 활성화
        isStunned.value = true;

        // 5초 뒤 해제
        setTimeout(() => { isStunned.value = false; }, 5000);
    };

    // 데이터 수신 리스너
    onMounted(() => {
        roomManager.onMessage((payload, senderId) => {
            // payload가 FLASHBANG 타입인지 확인
            // RoomManager 구현에 따라 payload 구조가 다를 수 있으므로 방어적 코딩
            const type = payload.topic || payload.type;
            const data = payload.data || payload;

            if (type === 'FLASHBANG') {
                const targetId = data.targetUserId || payload.targetUserId;
                
                // 내 ID와 타겟 ID가 일치하면 피격!
                if (targetId && targetId === authStore.userId) {
                    triggerStunEffect();
                }
            }
        });
    });

    return {
        isStunned,
        showSentFeedback,
        isModalOpen,
        drowsyParticipants,
        isWakeUpAvailable,
        openModal,
        closeModal,
        sendFlashbang,
        triggerStunEffect, // 테스트용 공개 메서드 (배포 시에는 제거 권장)
    };
}