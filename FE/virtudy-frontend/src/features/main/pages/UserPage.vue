<template>
  <GlobalBackground :skyType="1">
  <div class="min-h-screen relative w-full flex flex-col">
    
    <div class="flex-1 flex justify-center items-start pt-[8rem] pb-[8rem] px-[1rem] user-page-main">
      
      <div class="flex gap-[2rem] w-full max-w-[60rem] user-columns">
        
        <div class="w-[29.5rem] flex flex-col gap-[1rem] user-left">
          <UserProfile 
            class="aEffect_flipInX"
            :nick-name="userInfo.nickName"
            :tier-score="userInfo.tierScore"
            :tier="userInfo.tier"
            :favorite-room-title="userInfo.favoriteRoomTitle || '최애 스터디 없음'"
            :pure-study-time="userInfo.dailyPureStudyTime"
            :focus-depth="userInfo.dailyFocusDepth"
            @click-profile="handleProfileImageClick"
            @click-card="handleCardClick"
            :avatar-image-url="userInfo.avatarImageUrl"
            :avatar="userInfo.avatar"
          />
          
          <StudyMenu 
            class="animate-fade-in [animation-delay:0.3s]"
            @random-match="handleRandomMatch"
            @create-room="handleCreateRoom"
            @show-room-list="handleShowRoomList"
          />
        </div>
        
        <div class="w-[29.5rem] user-right">
          <RankingSectionMini 
            class="animate-fade-in [animation-delay:0.3s]"
            :private-top5="privateTop5"
            :team-top5="teamTop5"
            :is-loading="isLoading"
          />
        </div>
      </div>
    </div>
    
    <GlobalFooter />
  </div>
  </GlobalBackground>

  <MatchingModal
    v-if="isMatchingModalOpen"
    title-text="매칭 중..."
    subtitle-text="잠시만 기다려주세요..."
    @close="cancelRandomMatch"
  />
  <CreateRoomModal
    v-if="isCreateRoomModalOpen"
    @close="isCreateRoomModalOpen = false"
    @success="isCreateRoomModalOpen = false"
  />
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated } from 'vue'; 
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useStudyStore } from '@/stores/studyStore'; 
import { useUiStore } from '@/stores/uiStore'; // UI 스토어
import { useMainRanking } from '@/features/ranking/logic/useMainRanking';
import { getMyProfile } from '@/features/mypage/api/mypageApi';
import { lobbyAPI } from '@/features/lobby/api/lobbyAPI';
import type { UserProfileResponse } from '@/features/mypage/types/mypage.types';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import UserProfile from '@/shared/ui/UserProfile.vue'; 
import StudyMenu from '@/shared/ui/StudyMenu.vue';
import RankingSectionMini from '@/shared/ui/RankingSectionMini.vue';
import MatchingModal from '@/shared/ui/MatchingModal.vue';
import CreateRoomModal from '@/features/lobby/ui/CreateRoomModal.vue';
import GlobalBackground from '@/shared/ui/GlobalBackground.vue';

const router = useRouter();
const authStore = useAuthStore();
const studyStore = useStudyStore();
const uiStore = useUiStore(); // 스토어 사용

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
    await uiStore.openAlert('로그인이 필요합니다.', '알림');
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
    studyStore.setToken(data.liveKitToken, data.roomId);
    router.push({ name: 'StudyRoom', params: { roomId: data.roomId }, query: { from: 'user' } });
  } catch (error) {
    if (controller.signal.aborted || isCanceledError(error)) {
      return;
    }
    console.error('랜덤 매칭 실패:', error);
    const elapsed = Date.now() - startedAt;
    await delay(Math.max(0, 3000 - elapsed));
    if (controller.signal.aborted) return;
    
    await uiStore.openAlert('입장 가능한 방이 없습니다.', '알림');
  } finally {
    isMatchingModalOpen.value = false;
    matchingAbortController.value = null;
  }
};

const handleCreateRoom = async () => {
  if (!authStore.userId) {
    await uiStore.openAlert('로그인이 필요합니다.', '알림');
    return;
  }
  isCreateRoomModalOpen.value = true;
};
const handleShowRoomList = () => {
  router.push('/lobby');
};

// 카드 전체 클릭 핸들러 - 항상 마이페이지로 이동
const handleCardClick = () => {
  router.push('/mypage');
};

// 프로필 이미지 클릭 핸들러 - 아바타 없을 때만 작동
const handleProfileImageClick = async () => {
  // 아바타 데이터가 비어있는지 확인
  // (필수 파츠인 hairFront가 없으면 아바타가 없는 것으로 간주)
  const hasAvatar = userInfo.value.avatar && userInfo.value.avatar.hairFront;

  if (!hasAvatar) {
    const confirmed = await uiStore.openAlert(
      "아직 아바타가 없습니다.\n나만의 아바타를 만드시겠습니까? 🎨", 
      "아바타 생성"
    );
    
    if (confirmed) {
        router.push('/avatar/create');
    }
  }
  // 아바타가 있을 때는 아무것도 하지 않음 (카드 클릭 이벤트가 처리됨)
};


// 데이터 불러오는 함수 분리
const fetchUserData = async () => {
  // 랭킹 정보도 같이 갱신
  fetchTopRanks();
  
  try {
    const data = await getMyProfile();
    const hasAvatarConfig = data.avatar && Object.values(data.avatar).some((value) => Boolean(value));
    const mergedAvatar = hasAvatarConfig ? data.avatar : authStore.userInfo?.avatar;
    // 받아온 데이터로 userInfo 덮어쓰기
    // favoriteRoomTitle이 null이면 빈 문자열로 처리
    userInfo.value = {
      ...data,
      avatar: mergedAvatar,
      favoriteRoomTitle: data.favoriteRoomTitle || "" 
    };
    
    // AuthStore 동기화 (필요시)
    if (authStore.userInfo) {
      authStore.setUserInfo({
        ...authStore.userInfo,
        nickName: data.nickName,
        avatar: mergedAvatar,
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

// 페이지가 캐시되어 있다가 다시 활성화될 때도 데이터 갱신 (KeepAlive 사용 시 필수)
onActivated(() => {
  fetchUserData();
});
</script>

<style scoped>
@media (max-width: 1280px) {
  .user-page-main {
    padding-top: 6rem;
    padding-bottom: 6rem;
  }

  .user-columns {
    flex-direction: column;
    align-items: center;
    max-width: 100%;
  }

  .user-left,
  .user-right {
    width: min(95vw, 29.5rem);
  }
}

/* 애니메이션 */
@keyframes flipInX {
  from {
    transform: perspective(400px) rotate3d(1, 0, 0, 90deg);
    animation-timing-function: ease-in;
    opacity: 0;
  }

  40% {
    transform: perspective(400px) rotate3d(1, 0, 0, -20deg);
    animation-timing-function: ease-in;
  }

  60% {
    transform: perspective(400px) rotate3d(1, 0, 0, 10deg);
    opacity: 1;
  }

  80% {
    transform: perspective(400px) rotate3d(1, 0, 0, -5deg);
  }

  to {
    transform: perspective(400px);
  }
}

/* 애니메이션 클래스 정의 */
.aEffect_flipInX {
  -webkit-backface-visibility: visible !important;
  backface-visibility: visible !important;
  animation-name: flipInX;
  animation-duration: 1s; /* 속도 (1초) */
  animation-fill-mode: both; /* 끝나고 상태 유지 */
}

/* 부드럽게 나타나는 페이드인 애니메이션 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px); /* 아래에서 10px 정도 내려간 상태 */
  }
  to {
    opacity: 1;
    transform: translateY(0); /* 제자리로 */
  }
}

.animate-fade-in {
  animation: fadeInUp 0.8s ease-out forwards; /* 0.5초 동안 부드럽게 */
  opacity: 0; /* 시작 전엔 안 보이게 설정 */
}
</style>
