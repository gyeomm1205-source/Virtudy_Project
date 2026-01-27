<template>
  <div class="min-h-screen bg-[var(--color-cream2)] relative w-full">
    <GlobalNavBar />
    
    <div class="relative pt-[4.688rem] pb-[8rem] min-h-[calc(100vh-8rem)]">
      <div class="absolute left-[calc(8.33%+6.813rem)] top-[6.625rem] h-[39.688rem] w-[29.563rem]" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
        <div class="flex flex-col gap-0">
          
          <UserProfile 
            :nick-name="userInfo.nickName"
            :tier-score="userInfo.tierScore"
            :tier="userInfo.tier"
            :favorite-room-title="userInfo.favoriteRoomTitle"
            :pure-study-time="userInfo.pureStudyTime"
            :focus-depth="userInfo.focusDepth"
            :avatar-image-url="userInfo.avatarImageUrl"
          />
          
          <StudyMenu 
            @random-match="handleRandomMatch"
            @create-room="handleCreateRoom"
            @show-room-list="handleShowRoomList"
          />
        </div>
      </div>
      
      <div class="absolute left-[calc(50%+1.125rem)] top-[6.625rem] h-[39.688rem] w-[29.563rem]">
        <RankingSectionMini 
          :private-top5="privateTop5"
          :team-top5="teamTop5"
          :is-loading="isLoading"
        />
      </div>
    </div>
    
    <GlobalFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore'; 
// [추가] 랭킹 로직 훅 임포트
import { useMainRanking } from '@/features/ranking/logic/useMainRanking';

// [추가] 마이페이지 API 및 타입 import
import { getMyProfile } from '@/features/mypage/api/mypageApi';
import type { UserProfileResponse } from '@/features/mypage/types/mypage.types';

// UI 컴포넌트 임포트
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import UserProfile from '@/shared/ui/UserProfile.vue'; 
import StudyMenu from '@/shared/ui/StudyMenu.vue';
import RankingSectionMini from '@/shared/ui/RankingSectionMini.vue';

const router = useRouter();
const authStore = useAuthStore();

// [수정] userInfo 초기값 설정 (타입 안전성을 위해)
const userInfo = ref<UserProfileResponse>({
  nickName: "닉네임",
  email: "",
  jobType: "",
  tier: "BRONZE",
  avatarImageUrl: "",
  tierScore: 0,
  favoriteRoomTitle: "최애스터디",
  pureStudyTime: 0,
  focusDepth: 0,
});

// 랭킹 로직
const { privateTop5, teamTop5, isLoading, fetchTopRanks } = useMainRanking();

// 메뉴 핸들러
const handleRandomMatch = () => console.log("랜덤 매칭");
const handleCreateRoom = () => console.log("방 만들기");
const handleShowRoomList = () => console.log("전체 목록");

onMounted(async () => {
  // [수정] 1. 랭킹 데이터 Fetch 실행
  // 이 함수가 실행되면 privateTop5, teamTop5 변수에 데이터가 채워집니다.
  fetchTopRanks();

  // 2. 내 프로필 정보 API 호출
  try {
    const data = await getMyProfile();
    console.log('내 정보 전체:', data); 
    console.log('닉네임 필드 확인:', data.nickName);
    userInfo.value = data; 
    
    if (authStore.userInfo) {
       authStore.setUserInfo({
          ...authStore.userInfo,
          nickName: data.nickName,
          avatarImageUrl: data.avatarImageUrl
       });
    }
  } catch (error) {
    console.error("내 정보 불러오기 실패:", error);
    if (authStore.userInfo) {
      userInfo.value.nickName = authStore.userInfo.nickName;
    }
  }
});
</script>