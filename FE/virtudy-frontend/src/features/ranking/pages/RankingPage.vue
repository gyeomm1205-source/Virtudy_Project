<template>
  <div class="min-h-screen bg-[var(--color-syrup)] relative w-full">
    <!-- Global Navigation -->
    <GlobalNavBar />
    
    <!-- Main Content Area -->
    <div class="relative pt-[75px] pb-[200px] min-h-[calc(100vh-128px)]">
      <!-- Back Arrow -->
      <div class="absolute left-[77px] top-[39px] w-[54px] h-[54px] cursor-pointer hover:scale-110 transition-transform z-10" @click="goBack">
        <svg viewBox="0 0 54 54" class="w-full h-full" fill="var(--color-choco)">
          <path d="M40 22H18.8L29.4 11.4L27 9L13 23L27 37L29.4 34.6L18.8 24H40V22Z"/>
        </svg>
      </div>
      
      <!-- Left Side - Title -->
      <div class="absolute left-[59px] top-[244px] transform -translate-y-1/2">
        <h1 class="text-[var(--color-pancake)] text-[156px] font-['Ram'] font-medium leading-none tracking-[-18.72px]">
          랭킹
        </h1>
      </div>
      
      <!-- Character and Ranking Info -->
      <div class="absolute left-[calc(25%+31px)] top-[179px] w-[664px] h-[188px]">
        <!-- Profile Photo -->
        <div class="absolute left-[23px] top-[-7px] w-[146px] h-[146px] rounded-full overflow-hidden border-4 border-[var(--color-choco)]">
          <img 
            src="http://localhost:3845/assets/ae7ca0939b29738c16aee5cf86953e893d60c594.svg"
            alt="프로필 사진"
            class="w-full h-full object-cover"
          />
        </div>
        
        <!-- User Info Text -->
        <div class="absolute left-[162px] top-[30px]">
          <p class="text-[var(--color-choco)] text-[42px] font-['Ram'] font-medium leading-[48px] tracking-[-0.84px]">
            {{ myRankInfo?.nickName || '중꺾마' }}
          </p>
        </div>
        
        <div class="absolute left-[275px] top-[41px]">
          <p class="text-[var(--color-choco)] text-[32px] font-['Xcu'] font-normal leading-normal">
            님의 순위는...
          </p>
        </div>
        
        <!-- Rank Number -->
        <div class="absolute left-[371px] top-[58px]">
          <p class="text-[var(--color-butter)] text-[130px] font-['Xcu'] font-normal leading-normal">
            {{ myRankInfo?.rank || 223 }}위
          </p>
        </div>
      </div>
      
      <!-- Right Side - Ranking Section -->
      <div class="absolute left-[calc(8.33%+109px)] top-[139px] w-[982px]">
        <RankingSection 
          :personalRanking="personalRankingData"
          :teamRanking="teamRankingData"
          :currentUserEmail="currentUserEmail"
          @tabChange="handleTabChange"
          @search="handleSearch"
        />
      </div>
    </div>
    
    <!-- Global Footer -->
    <GlobalFooter />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../../stores/authStore';
import { useRanking } from '../logic/useRanking';
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import RankingSection from '@/shared/ui/RankingSection.vue';
// [수정] 타입 임포트 추가
import type { RankItem } from '../types/ranking.types';

const authStore = useAuthStore();
const router = useRouter();

const { 
  rankType, searchKeyword, rankList, myRankInfo, isLoading,
  currentPage, visiblePages, totalPages,
  changeType, changePage, handleSearch 
} = useRanking();

// Computed for current user email
const currentUserEmail = computed(() => authStore.userInfo?.email || '');

// Transform ranking data for RankingSection component - using dummy data for now
const personalRankingData = computed(() => {
  return [
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
  ];
});

const teamRankingData = computed(() => {
  return [
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
  ];
});

const goBack = () => {
  router.push({ name: 'user' }); 
};

// [수정] 본인 확인 함수 구현
// 리스트 아이템의 이메일과 로그인한 유저의 이메일을 비교합니다.
const isMyself = (item: RankItem) => {
  if (!authStore.userInfo?.email) return false;
  return item.email === authStore.userInfo.email;
};

// Handle events from RankingSection
const handleTabChange = (tab: string) => {
  changeType(tab);
};
</script>

