import { ref } from 'vue';
import { getTop5Rank } from '@/features/ranking/api/rankingApi';
import type { RankItem } from '@/features/ranking/types/ranking.types';

export const useMainRanking = () => {
  const privateTop5 = ref<RankItem[]>([]);
  const teamTop5 = ref<RankItem[]>([]);
  const isLoading = ref(true);

  const fetchTopRanks = async () => {
    isLoading.value = true;
    try {
      // 두 요청을 병렬로 동시에 실행 (로딩 속도 UP)
      const [privateData, teamData] = await Promise.all([
        getTop5Rank('private'), // 개인 랭킹 Top 5
        getTop5Rank('team')     // 팀 랭킹 Top 5
      ]);

      privateTop5.value = privateData;
      teamTop5.value = teamData;
    } catch (e) {
      console.error('Top 5 랭킹 로딩 실패:', e);
    } finally {
      isLoading.value = false;
    }
  };

  return {
    privateTop5,
    teamTop5,
    isLoading,
    fetchTopRanks, // 데이터 패칭 함수를 반환
  };
};