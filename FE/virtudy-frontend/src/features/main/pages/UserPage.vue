<template>
  <div class="min-h-screen bg-[var(--color-cream2)] relative w-full">
    <!-- Navigation -->
    <GlobalNavBar />
    
    <!-- Main Content Area -->
    <div class="relative pt-[4.688rem] pb-[8rem] min-h-[calc(100vh-8rem)]">
      <!-- 왼쪽 - 프로필과 메뉴 -->
      <div class="absolute left-[calc(8.33%+6.813rem)] top-[6.625rem] h-[39.688rem] w-[29.563rem]" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
        <div class="flex flex-col gap-0">
          <!-- 프로필 영역 -->
          <UserProfile 
            :user-nickname="userInfo.nickname"
            :user-score="userInfo.score"
            :user-tier="userInfo.tier"
            :favorite-study="userInfo.favoriteStudy"
            :study-hours="userInfo.studyHours"
            :concentration="userInfo.concentration"
          />
          
          <!-- 메뉴 바 -->
          <StudyMenu 
            @random-match="handleRandomMatch"
            @create-room="handleCreateRoom"
            @show-room-list="handleShowRoomList"
          />
        </div>
      </div>
      
      <!-- 오른쪽 - 랭킹 -->
      <div class="absolute left-[calc(50%+1.125rem)] top-[6.625rem] h-[39.688rem] w-[29.563rem]">
        <RankingSectionMini 
          :personal-ranking="personalRanking"
          :team-ranking="teamRanking"
        />
      </div>
    </div>
    
    <!-- Footer -->
    <GlobalFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import UserProfile from '@/shared/ui/UserProfile.vue';
import StudyMenu from '@/shared/ui/StudyMenu.vue';
import RankingSectionMini from '@/shared/ui/RankingSectionMini.vue';
import { useMainRanking } from '@/features/ranking/logic/useMainRanking';

const router = useRouter();

// 유저 정보 (추후 API나 스토어에서 가져올 수 있음)
const userInfo = ref({
  nickname: "닉네임",
  score: "000",
  tier: "티어명",
  favoriteStudy: "<최애스터디이름>",
  studyHours: "0",
  concentration: "00",
  profileImage: null,
});

// 랭킹 데이터 가져오기
const { privateTop5, teamTop5, isLoading } = useMainRanking();
import { onMounted } from 'vue';
import { useMainRanking } from '@/features/ranking/logic/useMainRanking';

// 로직 훅 실행. 이제 훅 자체는 API를 호출하지 않습니다.
const { privateTop5, teamTop5, isLoading, fetchTopRanks } = useMainRanking();

// onMounted 훅을 사용해, 컴포넌트가 완전히 준비된 후 API를 호출합니다.
onMounted(() => {
  fetchTopRanks();
});
</script>

