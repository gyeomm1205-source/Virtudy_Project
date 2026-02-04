// features/ranking/logic/useRanking.ts
import { ref, computed, onMounted } from 'vue';
import { getMyRank, getRankList } from '../api/rankingApi';
import type { RankItem, RankType, MyRankInfo } from '../types/ranking.types';
import { useAuthStore } from '@/stores/authStore';

export const useRanking = () => {
  // --- 상태 (State) ---
  const rankType = ref<RankType>('private');
  const searchKeyword = ref('');
  const allRankList = ref<RankItem[]>([]);
  const myRankInfo = ref<MyRankInfo | null>(null);
  const isLoading = ref(false);

  // 페이지 상태
  const currentPage = ref(0);
  const PAGE_SIZE = 10;
  const paginationSize = 5;

  // --- 기능 (Actions) ---
  const fetchMyRank = async () => {
    try {
      myRankInfo.value = await getMyRank(rankType.value);
    } catch (e) {
      console.error(e);
      console.warn('내 순위 데이터가 없습니다.', e);
      myRankInfo.value = null;
    }
  };

  const fetchAllPages = async () => {
    isLoading.value = true;
    try {
      const all: RankItem[] = [];
      const MAX_PAGES = 100;
      for (let page = 0; page < MAX_PAGES; page++) {
        const list = await getRankList(page, rankType.value);
        if (!list.length) break;
        all.push(...list);
        if (list.length < PAGE_SIZE) break;
      }
      allRankList.value = all;
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
    fetchAllPages();
  };

  const changePage = (page: number) => {
    if (page < 0 || page >= totalPages.value) return;
    currentPage.value = page;
  };

  const handleSearch = () => {
    currentPage.value = 0;
  };

  const filteredList = computed(() => {
    const query = searchKeyword.value.trim().toLowerCase();
    if (!query) return allRankList.value;
    return allRankList.value.filter((item) =>
      item.nickName.toLowerCase().includes(query)
    );
  });

  const totalPages = computed(() => {
    return Math.max(1, Math.ceil(filteredList.value.length / PAGE_SIZE));
  });

  const rankList = computed(() => {
    const start = currentPage.value * PAGE_SIZE;
    return filteredList.value.slice(start, start + PAGE_SIZE);
  });

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
    fetchAllPages();
  });

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
