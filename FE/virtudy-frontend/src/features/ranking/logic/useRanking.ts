// features/ranking/logic/useRanking.ts
import { ref, computed, onMounted } from 'vue';
import { getMyRank, getRankList, searchRank } from '../api/rankingApi';
import type { RankItem, RankType, MyRankInfo } from '../types/ranking.types';
import { useAuthStore } from '@/stores/authStore';

export const useRanking = () => {
  // --- 상태 (State) ---
  const rankType = ref<RankType>('private');
  const searchKeyword = ref('');
  const rankList = ref<RankItem[]>([]);
  const myRankInfo = ref<MyRankInfo | null>(null);
  const isLoading = ref(false);
  
  // 페이지네이션 상태
  const currentPage = ref(0);
  const totalPages = ref(10); 
  const paginationSize = 5;

  // --- 기능 (Actions) ---
  const fetchMyRank = async () => {
    try {
      myRankInfo.value = await getMyRank(rankType.value);
    } catch (e) {
      console.error(e);
      // [수정] 500 에러 등 실패 시 콘솔만 찍고 myRankInfo를 null로 유지
      // 이렇게 해야 화면에서 데이터 없음을 인지하고 대체 텍스트를 띄워줄 수 있음
      console.warn("신규 유저이거나 순위 데이터가 없습니다.", e);
      myRankInfo.value = null;
    }
  };

  const fetchList = async () => {
    isLoading.value = true;
    try {
      if (searchKeyword.value) {
        rankList.value = await searchRank(searchKeyword.value, rankType.value);
      } else {
        rankList.value = await getRankList(currentPage.value, rankType.value);
      }
    } catch (e) {
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  };

  const changeType = (type: RankType) => {
    rankType.value = type;
    currentPage.value = 0;
    searchKeyword.value = '';
    fetchMyRank();
    fetchList();
  };

  const changePage = (page: number) => {
    if (page < 0 || page >= totalPages.value) return;
    currentPage.value = page;
    fetchList();
  };

  const handleSearch = () => {
    currentPage.value = 0;
    fetchList();
  };

  // --- 계산된 속성 (Computed) ---
  const visiblePages = computed(() => {
    const start = Math.floor(currentPage.value / paginationSize) * paginationSize;
    const end = Math.min(start + paginationSize, totalPages.value);
    const pages = [];
    for (let i = start; i < end; i++) pages.push(i);
    return pages;
  });

  // 초기화
  onMounted(() => {
    fetchMyRank();
    fetchList();
  });

  // 템플릿에서 쓸 것들만 리턴
  return {
    rankType,
    searchKeyword,
    rankList,
    myRankInfo,
    isLoading,
    currentPage,
    totalPages,
    visiblePages,
    changeType,
    changePage,
    handleSearch
  };
};