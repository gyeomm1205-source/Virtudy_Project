<template>
  <div class="min-h-screen bg-[var(--color-cream2)] relative w-full flex flex-col">
    <GlobalNavBar />
    
    <div class="flex-1 flex justify-center items-start pt-[8rem] pb-[8rem] px-[1rem]">
      
      <div class="flex gap-[2rem] w-full max-w-[60rem]">
        
        <div class="w-[29.5rem] flex flex-col gap-[1rem]">
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
        
        <div class="w-[29.5rem]">
          <RankingSectionMini 
            :private-top5="privateTop5"
            :team-top5="teamTop5"
            :is-loading="isLoading"
          />
        </div>
      </div>
    </div>
    
    <GlobalFooter />
  </div>
</template>

<script setup lang="ts">
/* 스크립트 부분은 기존과 동일하므로 그대로 두시면 됩니다 */
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore'; 
import { useMainRanking } from '@/features/ranking/logic/useMainRanking';
import { getMyProfile } from '@/features/mypage/api/mypageApi';
import type { UserProfileResponse } from '@/features/mypage/types/mypage.types';

import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import UserProfile from '@/shared/ui/UserProfile.vue'; 
import StudyMenu from '@/shared/ui/StudyMenu.vue';
import RankingSectionMini from '@/shared/ui/RankingSectionMini.vue';

const router = useRouter();
const authStore = useAuthStore();

const userInfo = ref<UserProfileResponse>({
  nickName: "닉네임",
  email: "",
  jobType: "",
  tier: "BRONZE",
  avatar: {
    hairFront: "",
    hairBack: "",
    hairColor: "",
    eyes: "",
    glasses: "",
    outfit: "",
    clothesColor: "",
  },
  tierScore: 0,
  favoriteRoomTitle: "최애스터디",
  pureStudyTime: 0,
  focusDepth: 0,
  avatarImageUrl: "",
});

const { privateTop5, teamTop5, isLoading, fetchTopRanks } = useMainRanking();

const handleRandomMatch = () => console.log("랜덤 매칭");
const handleCreateRoom = () => console.log("방 만들기");
const handleShowRoomList = () => {
  router.push('/lobby');
};

onMounted(async () => {
  fetchTopRanks();
  try {
    const data = await getMyProfile();
    userInfo.value = data; 
    
    if (authStore.userInfo) {
       authStore.setUserInfo({
         ...authStore.userInfo,
         nickName: data.nickName,
         avatar: data.avatar,
         avatarImageUrl: data.avatarImageUrl ?? authStore.userInfo.avatarImageUrl,
       });
    }
  } catch (error) {
    if (authStore.userInfo) {
      userInfo.value.nickName = authStore.userInfo.nickName;
    }
  }
});
</script>