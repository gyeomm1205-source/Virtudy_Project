<template>
  <div class="bg-[var(--color-cream2)] relative w-full h-screen">
    <!-- Navigation -->
    <GlobalNavBar />
    
    <!-- Footer -->
    <GlobalFooter />
    
    <!-- 왼쪽 - 프로필과 메뉴 -->
    <div class="absolute left-[calc(8.33%+109px)] top-[106px] h-[635px] w-[473px]" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
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
    <div class="absolute left-[calc(50%+18px)] top-[106px] bg-[var(--color-syrup)] border-2 border-[var(--color-choco)] border-solid h-[635px] w-[473px] overflow-clip" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
      <RankingSection 
        :personal-ranking="personalRanking"
        :team-ranking="teamRanking"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import UserProfile from '@/shared/ui/UserProfile.vue';
import StudyMenu from '@/shared/ui/StudyMenu.vue';
import RankingSection from '@/shared/ui/RankingSection.vue';
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

// 개인 랭킹 데이터 변환
const personalRanking = computed(() => {
  if (!privateTop5.value) return [];
  return privateTop5.value.map(item => ({
    nickname: item.nickName,
    score: item.score.toString(),
  }));
});

// 팀 랭킹 데이터 변환  
const teamRanking = computed(() => {
  if (!teamTop5.value) return [];
  return teamTop5.value.map(item => ({
    teamName: item.nickName, // 팀명으로 사용
    score: item.score.toString(),
  }));
});

// StudyMenu 이벤트 핸들러
const handleRandomMatch = () => {
  console.log('랜덤 매칭 요청');
  // 랜덤 매칭 로직 구현
};

const handleCreateRoom = () => {
  console.log('방 만들기 요청');
  // 방 만들기 페이지로 이동하거나 모달 열기
};

const handleShowRoomList = () => {
  router.push('/lobby');
};
</script>