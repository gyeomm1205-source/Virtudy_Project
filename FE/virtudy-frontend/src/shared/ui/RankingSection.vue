<template>
  <div class="w-[982px] h-[658px]">
    <!-- 탭 버튼들 -->
    <div class="h-[62px] relative mb-[2px]">
      <div class="absolute left-[31px] top-[9px] flex gap-0">
        <button 
          @click="setCurrentTab('private')"
          :class="[
            'border-2 border-[var(--color-choco)] border-solid px-[32px] py-[20px] rounded-tl-[30px] rounded-tr-[30px] rounded-bl-[2px] rounded-br-[2px]',
            currentTab === 'private' ? 'bg-[var(--color-butter)]' : 'bg-[var(--color-cream)]'
          ]"
          style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
        >
          <span class="text-[var(--color-choco)] text-[24px] font-['PfStardust30S'] font-normal leading-none">
            개인
          </span>
        </button>
        <button 
          @click="setCurrentTab('team')"
          :class="[
            'border-2 border-[var(--color-choco)] border-solid px-[32px] py-[20px] rounded-tl-[30px] rounded-tr-[30px] rounded-bl-[2px] rounded-br-[2px]',
            currentTab === 'team' ? 'bg-[var(--color-butter)]' : 'bg-[var(--color-cream)]'
          ]"
          style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
        >
          <span class="text-[var(--color-choco)] text-[24px] font-['PfStardust30S'] font-normal leading-none">
            팀
          </span>
        </button>
      </div>
      
      <!-- 검색바 -->
      <div class="absolute right-0 top-[14px] w-[393px] border-2 border-[var(--color-choco)] border-solid bg-[var(--color-cream2)] h-[33px] flex items-center px-[7px] gap-[10px]" style="box-shadow: 3.667px 3.667px 0px 0px var(--color-choco);">
        <div class="w-[21px] h-[21px] flex items-center justify-center">
          <svg viewBox="0 0 21 21" class="w-[19.636px] h-[19.636px]">
            <path d="M8 2C11.314 2 14 4.686 14 8C14 9.248 13.587 10.397 12.897 11.324L18.707 17.071C19.098 17.461 19.098 18.095 18.707 18.485C18.317 18.876 17.683 18.876 17.293 18.485L11.486 12.678C10.559 13.368 9.41 13.781 8.162 13.781C4.848 13.781 2.162 11.095 2.162 7.781C2.162 4.467 4.848 1.781 8.162 1.781Z" fill="var(--color-choco)"/>
          </svg>
        </div>
        <input 
          v-model="searchQuery"
          type="text" 
          placeholder="Search"
          class="flex-1 bg-transparent border-none outline-none text-[var(--color-syrup)] text-[18px] font-['PfStardust30S'] font-normal leading-normal tracking-[-0.7px]"
          @input="onSearch"
        />
      </div>
    </div>
    
    <!-- 랭킹 목록 -->
    <div class="border-2 border-[var(--color-choco)] border-solid h-[600px] w-[981px] rounded-[20px] overflow-clip flex flex-col items-center justify-center" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
      <div class="w-full h-full flex flex-col">
        <div 
          v-for="(item, index) in displayedRanking" 
          :key="`ranking-${index}`"
          class="flex-1 relative w-full"
          :class="[
            index % 2 === 0 ? 'bg-[var(--color-choco)]' : 'bg-[var(--color-cream)]',
            { 'bg-[var(--color-choco)] border-2 border-[var(--color-butter)] text-[var(--color-butter)]': isMyself(item) }
          ]"
        >
          <!-- 순위 -->
          <div class="absolute left-[27px] top-1/2 transform -translate-y-1/2 w-[31px]">
            <p 
              class="text-[28px] font-['Xcu'] font-normal leading-normal"
              :class="[
                isMyself(item) ? 'text-[var(--color-butter)]' : 
                index % 2 === 0 ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'
              ]"
            >
              {{ item.rank || (index + 1) }}
            </p>
          </div>
          
          <!-- 닉네임/팀명 -->
          <div class="absolute left-[491px] top-1/2 transform -translate-x-1/2 -translate-y-1/2">
            <p 
              class="text-[24px] font-['PfStardust30S'] font-normal leading-none text-center w-[202px] truncate"
              :class="[
                isMyself(item) ? 'text-[var(--color-butter)]' : 
                (index % 2 === 0 && index < 3) || (index % 2 === 1 && index === 1) || (index % 2 === 0 && index === 6) || (index % 2 === 0 && index === 8) ? 'text-[var(--color-butter)]' : 'text-[var(--color-pancake)]'
              ]"
            >
              {{ getName(item) }}
            </p>
          </div>
          
          <!-- 점수 -->
          <div class="absolute left-[873.5px] top-1/2 transform -translate-x-1/2 -translate-y-1/2">
            <p 
              class="text-[24px] font-['PfStardust30S'] font-normal leading-none text-center w-[97px]"
              :class="[
                isMyself(item) ? 'text-[var(--color-butter)]' : 
                (index % 2 === 0 && index < 3) || (index % 2 === 1 && index === 1) || (index % 2 === 0 && index === 6) || (index % 2 === 0 && index === 8) ? 'text-[var(--color-butter)]' : 'text-[var(--color-pancake)]'
              ]"
            >
              {{ item.score }}p
            </p>
          </div>
          
          <!-- 왕관 아이콘 -->
          <div class="absolute right-[25px] top-1/2 transform -translate-y-1/2 w-[24px] h-[24px]">
            <svg viewBox="0 0 24 24" class="w-full h-full">
              <path 
                d="M5 16L3 8L9 10L12 6L15 10L21 8L19 16H5Z" 
                :fill="(index % 2 === 0 && index < 3) || (index % 2 === 1 && index === 1) || (index % 2 === 0 && index === 6) || (index % 2 === 0 && index === 8) ? 'var(--color-butter)' : 'var(--color-syrup)'"
                stroke="var(--color-choco)" 
                stroke-width="1"
              />
            </svg>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 페이지네이션 -->
    <div class="h-[28px] mt-[20px] flex items-center justify-center relative">
      <div class="flex items-center gap-[24px]">
        <!-- 맨 앞으로 -->
        <button 
          @click="goToFirstPage" 
          :disabled="currentPage === 0"
          class="w-[28px] h-[28px] relative disabled:opacity-50"
        >
          <svg viewBox="0 0 28 28" class="w-full h-full">
            <rect width="28" height="28" fill="var(--color-choco)" opacity="0.3"/>
          </svg>
          <svg viewBox="0 0 20 20" class="absolute inset-1">
            <path d="M15 5L10 10L15 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
            <path d="M11 5L6 10L11 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
          </svg>
        </button>
        
        <!-- 이전 페이지 -->
        <button 
          @click="goToPrevPage" 
          :disabled="currentPage === 0"
          class="w-[28px] h-[28px] relative disabled:opacity-50"
        >
          <svg viewBox="0 0 28 28" class="w-full h-full">
            <rect width="28" height="28" fill="var(--color-choco)" opacity="0.3"/>
          </svg>
          <svg viewBox="0 0 20 20" class="absolute inset-1">
            <path d="M12 5L7 10L12 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
          </svg>
        </button>
        
        <!-- 페이지 번호들 -->
        <div class="flex items-center gap-[12px]">
          <button 
            v-for="pageNum in visiblePages" 
            :key="pageNum"
            @click="goToPage(pageNum)"
            class="w-[28px] h-[28px] flex items-center justify-center"
          >
            <span 
              :class="[
                'text-[20px] font-[PfStardust30S] font-normal leading-none tracking-[-0.8px] text-center',
                pageNum === currentPage ? 'text-[var(--color-butter)]' : 'text-[var(--color-choco)]'
              ]"
              :style="'text-shadow: 1px 1px 0px var(--color-butter2);'"
            >
              {{ pageNum + 1 }}
            </span>
          </button>
        </div>
        
        <!-- 다음 페이지 -->
        <button 
          @click="goToNextPage" 
          :disabled="currentPage >= totalPages - 1"
          class="w-[28px] h-[28px] relative disabled:opacity-50"
        >
          <svg viewBox="0 0 28 28" class="w-full h-full">
            <rect width="28" height="28" fill="var(--color-choco)" opacity="0.3"/>
          </svg>
          <svg viewBox="0 0 20 20" class="absolute inset-1">
            <path d="M8 5L13 10L8 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
          </svg>
        </button>
        
        <!-- 맨 뒤로 -->
        <button 
          @click="goToLastPage" 
          :disabled="currentPage >= totalPages - 1"
          class="w-[28px] h-[28px] relative disabled:opacity-50"
        >
          <svg viewBox="0 0 28 28" class="w-full h-full">
            <rect width="28" height="28" fill="var(--color-choco)" opacity="0.3"/>
          </svg>
          <svg viewBox="0 0 20 20" class="absolute inset-1">
            <path d="M5 5L10 10L5 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
            <path d="M9 5L14 10L9 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>
          
<script setup lang="ts">
import { ref, computed, watch } from 'vue';

interface RankingItem {
  rank?: number;
  nickname?: string;
  teamName?: string;
  score: string;
  email?: string;
}

interface RankingSectionProps {
  personalRanking?: RankingItem[];
  teamRanking?: RankingItem[];
  currentUserEmail?: string;
}

// Props 정의
const props = withDefaults(defineProps<RankingSectionProps>(), {
  personalRanking: () => [
    { rank: 1, nickname: "스터디킹", score: "2450", email: "user1@example.com" },
    { rank: 2, nickname: "집중력마스터", score: "2398", email: "user2@example.com" },
    { rank: 3, nickname: "공부하는곰", score: "2267", email: "user3@example.com" },
    { rank: 4, nickname: "알고리즘러버", score: "2156", email: "user4@example.com" },
    { rank: 5, nickname: "백준킬러", score: "2089", email: "user5@example.com" },
    { rank: 6, nickname: "코딩천재", score: "1998", email: "user6@example.com" },
    { rank: 7, nickname: "디버깅마스터", score: "1967", email: "user7@example.com" },
    { rank: 8, nickname: "스프링부트", score: "1945", email: "user8@example.com" },
    { rank: 9, nickname: "리액트마스터", score: "1893", email: "user9@example.com" },
    { rank: 10, nickname: "자바의신", score: "1845", email: "user10@example.com" },
    { rank: 11, nickname: "파이썬짱", score: "1789", email: "user11@example.com" },
    { rank: 12, nickname: "노드제이에스", score: "1756", email: "user12@example.com" },
    { rank: 13, nickname: "타입스크립트", score: "1723", email: "user13@example.com" },
    { rank: 14, nickname: "데이터베이스", score: "1698", email: "user14@example.com" },
    { rank: 15, nickname: "네트워크킹", score: "1645", email: "user15@example.com" },
    { rank: 16, nickname: "클라우드마스터", score: "1612", email: "user16@example.com" },
    { rank: 17, nickname: "도커왕", score: "1589", email: "user17@example.com" },
    { rank: 18, nickname: "쿠버네티스", score: "1567", email: "user18@example.com" },
    { rank: 19, nickname: "마이크로서비스", score: "1534", email: "user19@example.com" },
    { rank: 20, nickname: "빅데이터분석가", score: "1512", email: "user20@example.com" },
  ],
  teamRanking: () => [
    { rank: 1, teamName: "알고리즘마스터즈", score: "8950", email: "team1@example.com" },
    { rank: 2, teamName: "코딩엘리트", score: "8756", email: "team2@example.com" },
    { rank: 3, teamName: "스터디킹덤", score: "8623", email: "team3@example.com" },
    { rank: 4, teamName: "백엔드크루", score: "8489", email: "team4@example.com" },
    { rank: 5, teamName: "프론트엔드팀", score: "8356", email: "team5@example.com" },
    { rank: 6, teamName: "풀스택개발자", score: "8234", email: "team6@example.com" },
    { rank: 7, teamName: "데이터사이언티스트", score: "8123", email: "team7@example.com" },
    { rank: 8, teamName: "머신러닝팀", score: "7998", email: "team8@example.com" },
    { rank: 9, teamName: "클라우드팀", score: "7876", email: "team9@example.com" },
    { rank: 10, teamName: "데브옵스크루", score: "7756", email: "team10@example.com" },
  ],
  currentUserEmail: '',
});

// Emits 정의
const emit = defineEmits<{
  tabChange: [tab: string];
  search: [query: string];
}>();

// State
const currentTab = ref<string>('private');
const searchQuery = ref<string>('');
const currentPage = ref<number>(0);
const ITEMS_PER_PAGE = 10;

// Computed
const currentRanking = computed(() => {
  return currentTab.value === 'private' ? props.personalRanking : props.teamRanking;
});

const filteredRanking = computed(() => {
  let filtered = [...currentRanking.value];
  
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(item => {
      const name = getName(item).toLowerCase();
      return name.includes(query);
    });
  }
  
  return filtered;
});

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredRanking.value.length / ITEMS_PER_PAGE));
});

const displayedRanking = computed(() => {
  const start = currentPage.value * ITEMS_PER_PAGE;
  const end = start + ITEMS_PER_PAGE;
  return filteredRanking.value.slice(start, end);
});

const visiblePages = computed(() => {
  const pages: number[] = [];
  const start = Math.max(0, currentPage.value - 2);
  const end = Math.min(totalPages.value - 1, currentPage.value + 2);
  
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }
  
  return pages;
});

// Methods
const setCurrentTab = (tab: string) => {
  currentTab.value = tab;
  currentPage.value = 0;
  emit('tabChange', tab);
};

const onSearch = () => {
  currentPage.value = 0;
  emit('search', searchQuery.value);
};

const getName = (item: RankingItem) => {
  return item.nickname || item.teamName || '';
};

const isMyself = (item: RankingItem) => {
  return item.email === props.currentUserEmail;
};

const goToPage = (page: number) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page;
  }
};

const goToFirstPage = () => {
  currentPage.value = 0;
};

const goToLastPage = () => {
  currentPage.value = totalPages.value - 1;
};

const goToPrevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--;
  }
};

const goToNextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++;
  }
};

// Watch for tab changes
watch(() => props.personalRanking, () => {
  currentPage.value = 0;
});

watch(() => props.teamRanking, () => {
  currentPage.value = 0;
});
</script>