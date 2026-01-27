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

// 개인 랭킹 데이터 변환 - 더미 데이터 사용
const personalRanking = computed(() => {
  return [
    { rank: 1, nickname: "피그마는너무어려워", score: "2450" },
    { rank: 2, nickname: "그래도할만해요", score: "2398" },
    { rank: 3, nickname: "3팀화이팅", score: "2267" },
    { rank: 4, nickname: "아니진짜어렵다니깐", score: "2156" },
    { rank: 5, nickname: "오늘처음써봐요", score: "2089" },
  ];
});

// 팀 랭킹 데이터 변환 - 더미 데이터 사용
const teamRanking = computed(() => {
  return [
    { rank: 1, teamName: "3팀알고리즘", score: "8950" },
    { rank: 2, teamName: "3팀자료구조", score: "8756" },
    { rank: 3, teamName: "3팀컴퓨터사이언스", score: "8623" },
    { rank: 4, teamName: "5J1P", score: "8489" },
    { rank: 5, teamName: "버터디스터디", score: "8356" },
  ];
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