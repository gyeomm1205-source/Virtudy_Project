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
import { useAuthStore } from '../../../stores/authStore';
import { useRanking } from '../logic/useRanking';
import { useRouter } from 'vue-router';
// [수정] 타입 임포트 추가
import type { RankItem } from '../types/ranking.types';

const authStore = useAuthStore();
const router = useRouter();

const { 
  rankType, searchKeyword, rankList, myRankInfo, isLoading,
  currentPage, visiblePages, totalPages,
  changeType, changePage, handleSearch 
} = useRanking();

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

