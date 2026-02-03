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
            :favorite-room-title="userInfo.favoriteRoomTitle || '최애 스터디 없음'"
            :pure-study-time="userInfo.dailyPureStudyTime"
            :focus-depth="userInfo.dailyFocusDepth"
            @click-profile="handleProfileClick"
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

  <MatchingModal v-if="isMatchingModalOpen" @close="cancelRandomMatch" />
  <CreateRoomModal
    v-if="isCreateRoomModalOpen"
    @close="isCreateRoomModalOpen = false"
    @success="isCreateRoomModalOpen = false"
  />
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated } from 'vue'; // [추가] onActivated
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore'; 
import { useMainRanking } from '@/features/ranking/logic/useMainRanking';
import { getMyProfile } from '@/features/mypage/api/mypageApi';
import { lobbyAPI } from '@/features/lobby/api/lobbyAPI';
import type { UserProfileResponse } from '@/features/mypage/types/mypage.types';

import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import UserProfile from '@/shared/ui/UserProfile.vue'; 
import StudyMenu from '@/shared/ui/StudyMenu.vue';
import RankingSectionMini from '@/shared/ui/RankingSectionMini.vue';
import MatchingModal from '@/shared/ui/MatchingModal.vue';
import CreateRoomModal from '@/features/lobby/ui/CreateRoomModal.vue';

const router = useRouter();
const authStore = useAuthStore();

const userInfo = ref<UserProfileResponse>({
  userId: "",
  nickName: "",
  email: "",
  jobType: "",
  tier: "",
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
  favoriteRoomTitle: "",
  dailyPureStudyTime: 0,
  dailyFocusDepth: 0,
  avatarImageUrl: "",
});

const { privateTop5, teamTop5, isLoading, fetchTopRanks } = useMainRanking();

const isMatchingModalOpen = ref(false);
const matchingAbortController = ref<AbortController | null>(null);
const isCreateRoomModalOpen = ref(false);

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

const isCanceledError = (error: unknown) => {
  const err = error as { code?: string; name?: string } | null;
  return err?.code === 'ERR_CANCELED' || err?.name === 'CanceledError';
};

const cancelRandomMatch = () => {
  if (matchingAbortController.value) {
    matchingAbortController.value.abort();
    matchingAbortController.value = null;
  }
  isMatchingModalOpen.value = false;
};

const handleRandomMatch = async () => {
  if (isMatchingModalOpen.value) return;
  if (!authStore.userId) {
    alert('로그인이 필요합니다.');
    return;
  }

  isMatchingModalOpen.value = true;
  const startedAt = Date.now();
  const controller = new AbortController();
  matchingAbortController.value = controller;

  try {
    const data = await lobbyAPI.enterRandomRoom(authStore.userId, controller.signal);
    if (controller.signal.aborted) return;
    const elapsed = Date.now() - startedAt;
    await delay(Math.max(0, 3000 - elapsed));
    if (controller.signal.aborted) return;
    router.push(`/study/${data.userId}?token=${data.liveKitToken}`);
  } catch (error) {
    if (controller.signal.aborted || isCanceledError(error)) {
      return;
    }
    console.error('랜덤 매칭 실패:', error);
    const elapsed = Date.now() - startedAt;
    await delay(Math.max(0, 3000 - elapsed));
    if (controller.signal.aborted) return;
    alert('입장 가능한 방이 없습니다.');
  } finally {
    isMatchingModalOpen.value = false;
    matchingAbortController.value = null;
  }
};
const handleCreateRoom = () => {
  if (!authStore.userId) {
    alert('로그인이 필요합니다.');
    return;
  }
  isCreateRoomModalOpen.value = true;
};
const handleShowRoomList = () => {
  router.push('/lobby');
};

// 아바타 클릭 핸들러
const handleProfileClick = () => {
  // 아바타 데이터가 비어있는지 확인
  // (필수 파츠인 hairFront가 없으면 아바타가 없는 것으로 간주)
  const hasAvatar = userInfo.value.avatar && userInfo.value.avatar.hairFront;

  if (!hasAvatar) {
    // 아바타가 없으면 생성 페이지로 이동
    if (confirm("아직 아바타가 없습니다. 나만의 아바타를 만드시겠습니까? 🎨")) {
        router.push('/avatar/create');
    }
  } else {
    console.log("이미 아바타가 있습니다.");
  }
};


// 데이터 불러오는 함수 분리
const fetchUserData = async () => {
  // [추가] 랭킹 정보도 같이 갱신
  fetchTopRanks();
  
  try {
    const data = await getMyProfile();
    // [중요] 받아온 데이터로 userInfo 덮어쓰기
    // favoriteRoomTitle이 null이면 빈 문자열로 처리
    userInfo.value = {
      ...data,
      favoriteRoomTitle: data.favoriteRoomTitle || "" 
    };
    
    // AuthStore 동기화 (필요시)
    if (authStore.userInfo) {
       authStore.setUserInfo({
         ...authStore.userInfo,
         nickName: data.nickName,
         avatar: data.avatar,
         avatarImageUrl: data.avatarImageUrl ?? authStore.userInfo.avatarImageUrl,
       });
    }
  } catch (error) {
    console.error("프로필 로딩 실패:", error);
    // 에러 시 처리 (기존 로직 유지)
  }
};

onMounted(() => {
  fetchUserData();
});

// [추가] 페이지가 캐시되어 있다가 다시 활성화될 때도 데이터 갱신 (KeepAlive 사용 시 필수)
onActivated(() => {
  fetchUserData();
});
</script>