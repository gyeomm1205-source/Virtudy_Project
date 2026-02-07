<template>
  <GlobalBackground :skyType="3">
  <div class="relative min-h-[85rem] w-full pb-[8rem] mypage-root">
    
    <div class="absolute left-[4.75rem] top-[22.25rem] -translate-y-1/2">
      <h1 class="text-[var(--color-pancake)] [text-shadow:4px_4px_0px_var(--color-choco)] text-[9.75rem] font-['Ram'] font-medium leading-none tracking-[-1.17rem] whitespace-nowrap">
        마이<br />페이지
      </h1>
    </div>

    <button 
      @click="goBack"
      class="absolute left-[4.75rem] top-[7.4375rem] w-[4rem] h-[4rem] cursor-pointer hover:scale-110 transition-transform"
    >
      <svg 
        xmlns="http://www.w3.org/2000/svg" 
        fill="none" 
        viewBox="0 0 24 24" 
        stroke-width="1.5" 
        stroke="var(--color-choco)" 
        class="w-full h-full"
      >
        <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
      </svg>
    </button>

    <div class="absolute left-[calc(8.33%+5.875rem)] top-[16.875rem] w-[15.9375rem] h-[30.25rem] mypage-menu">
      <div class="absolute top-[16.25rem] left-0 w-full flex flex-col gap-[12px] mypage-menu-buttons">
        <button 
          @click="activeTab = 'profile'"
          :class="[
            'w-full px-[2rem] py-[1.25rem] border-2 border-[var(--color-choco)] border-solid rounded-[0.125rem] shadow-[4px_4px_0px_0px_var(--color-choco)] cursor-pointer transition-transform hover:scale-105',
            activeTab === 'profile' ? 'bg-[var(--color-butter)]' : 'bg-[var(--color-butter2)]'
          ]"
        >
          <span class="text-[var(--color-choco)] text-[1.75rem] font-['Xcu'] font-medium leading-none">
            프로필
          </span>
        </button>
        <button 
          @click="goToReport"
          :class="[
            'w-full px-[2rem] py-[1.25rem] border-2 border-[var(--color-choco)] border-solid rounded-[0.125rem] shadow-[4px_4px_0px_0px_var(--color-choco)] cursor-pointer transition-transform hover:scale-105',
            activeTab === 'report' ? 'bg-[var(--color-butter)]' : 'bg-[var(--color-butter2)]'
          ]"
        >
          <span class="text-[var(--color-choco)] text-[1.75rem] font-['Xcu'] font-medium leading-none">
            리포트
          </span>
        </button>
      </div>
    </div>

    <div class="absolute left-[calc(33.33%+0.3125rem)] top-[6.8125rem] w-[45.75rem] h-[62.5625rem] mypage-content">
      <div class="bg-[var(--color-syrup)] border-2 border-[var(--color-choco)] border-solid h-full w-full rounded-[1.25rem] overflow-hidden relative shadow-[4px_4px_0px_0px_var(--color-choco)] mypage-card">
        <button 
          @click="openEditModal"
          class="absolute right-[3rem] top-[1.1875rem] bg-[var(--color-cream2)] border-2 border-[var(--color-choco)] border-solid px-[2.2rem] py-[0.8rem] rounded-[1.875rem] shadow-[4px_4px_0px_0px_var(--color-choco)] cursor-pointer hover:scale-105 transition-transform"
        >
          <span class="text-[var(--color-choco)] text-[1.5rem] font-['PfStardust30S'] font-normal leading-none">
            회원정보수정
          </span>
        </button>
        <div class="absolute left-0 top-[4.5rem] w-full flex flex-col items-center">
          <div class="flex items-center justify-center text-[2rem] relative w-[10.5rem] h-[10.5rem] mb-[0.5rem] translate-y-[-0.1rem] translate-x-[0.2rem]">
            <div class="w-[9rem] h-[9rem] rounded-full bg-[var(--color-butter)] relative overflow-hidden shadow-md mx-auto mt-[1.5rem] transform-gpu">
              <CharacterAvatar 
                v-if="hasAvatarConfig"
                :config="userInfo!.avatar!"
                class="absolute inset-0 w-full h-full object-cover scale-[1.45] origin-center translate-y-[58%] translate-x-[1.45rem]"
              />
              <img 
                v-else-if="userInfo?.avatarImageUrl" 
                :src="userInfo.avatarImageUrl" 
                alt="프로필 사진"
                class="absolute inset-0 w-full h-full object-cover scale-110 origin-center"
              />
              <div v-else @click="goToAvatarCreate"class="w-full h-full flex flex-col items-center justify-center hover:bg-[#FFE08C] transition-colors cursor-pointer group">
                <span class="text-[var(--color-choco)] font-bold text-[1.1rem] font-['Xcu'] leading-tight text-center">
                  아바타<br>생성하기
                </span>
              </div>
            </div>
          </div>
          
          <h2 class="text-[var(--color-choco)] text-[1.75rem] font-['Xcu'] font-medium leading-none mb-[0.3rem] text-center max-w-[24rem] break-words">
            {{ userInfo?.nickName || '닉네임' }}
          </h2>
          
          <div class="flex items-center gap-[1rem] mb-[-0.2rem] justify-center">
            <span class="text-[var(--color-pancake)] text-[1.5rem] font-['PfStardust30S'] font-normal leading-none">
              {{ userInfo?.tierScore || 0 }}p
            </span>
            <span class="text-[var(--color-pancake)] text-[1.5rem] font-['PfStardust30S'] font-normal leading-none">
              {{ userInfo?.tier || '티어명' }}
            </span>
          </div>
          
          <p class="text-[var(--color-choco)] text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center max-w-[28rem] break-words">
            &lt;{{ userInfo?.favoriteRoomTitle || '최애 스터디 없음' }}&gt;
          </p>
        </div>

        <div class="absolute left-0 top-[22.625rem] w-full px-[1.25rem]">
          <div class="flex items-end justify-between mb-[0.5rem]">
            <h3 class="text-[var(--color-butter)] text-[2.625rem] font-['Ram'] font-medium leading-[3rem] tracking-[-0.0525rem] ml-[1rem]">
            미니 리포트
          </h3>
          </div>
    
          <div class="mb-[1.25rem] mini-report-wrapper">
            <MiniReport 
              class="gap-[12.5rem] h-[8.9375rem] px-[12.75rem] py-[1.125rem]"
              :studyTime="displayPureStudyTime" 
              :focusing="displayFocusDepth" 
            />
            <!-- 백엔드에서 해결되면 다시 사용해야됨 -->
            <!-- :studyTime="userInfo?.dailyPureStudyTime"
            :focusing="userInfo?.dailyFocusDepth" -->
          </div>
          
          <div class="mb-[1.25rem] relative">
            <PentagonChart
              :endurance="reportData?.endurance || 0"
              :focusDepth="reportData?.focusDepth || 0"
              :regularity="reportData?.regularity || 0"
              :stability="reportData?.stability || 0"
              :willPower="reportData?.willPower || 0"
            />
            <div
              v-if="!hasReport && !isLoading"
              class="absolute inset-0 z-10 flex items-center justify-center rounded-[20px] bg-[rgba(255,253,245,0.7)] backdrop-blur-[2px]"
            >
              <p class="text-[var(--color-choco)] text-[1.4rem] font-['PfStardust30S'] text-center px-6 leading-snug">
                첫 주간 리포트를 준비 중이에요. 오늘의 공부부터 시작해볼까요?
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <button
      @click="handleWithdraw"
      class="absolute left-[calc(83.33%-38px)] top-[70.2rem] text-[var(--color-syrup)] text-[0.75rem] font-['Pretendard'] font-normal leading-none tracking-[-0.03rem] underline cursor-pointer"
    >
      회원탈퇴
    </button>

    <div class="absolute bottom-0 left-0 w-full z-50">
      <GlobalFooter />
    </div>
    
    <ProfileEditModal 
      v-if="isEditModalOpen"
      :email="userInfo?.email || ''"
      :avatar="userInfo?.avatar"
      :avatar-image-url="userInfo?.avatarImageUrl"
      v-model:nickName="editForm.nickName"
      v-model:jobType="editForm.jobType"
      :jobOptions="JOB_OPTIONS"
      @close="closeEditModal"
      @submit="submitEdit"
    />
  </div>
  </GlobalBackground>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useMyPage } from '../logic/useMyPage';
import { JOB_OPTIONS } from '../types/mypage.types';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import MiniReport from '@/shared/ui/MiniReport.vue';
import PentagonChart from '@/shared/ui/PentagonChart.vue';  
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import ProfileEditModal from '../ui/ProfileEditModal.vue';
import { useWeeklyReport } from  '@/features/report/logic/useWeeklyReport';
import { authAPI } from '@/features/auth/api/authAPI';
import { useAuthStore } from '@/stores/authStore';
import { useUiStore } from '@/stores/uiStore';
import GlobalBackground from '@/shared/ui/GlobalBackground.vue';

const router = useRouter();
const authStore = useAuthStore();
const uiStore = useUiStore(); // [추가]

const { 
  userInfo, activeTab, isEditModalOpen, editForm, 
  openEditModal, closeEditModal, submitEdit 
} = useMyPage();

// useWeeklyReport 내부의 onMounted가 실행되면서 자동으로 '지난주' 데이터를 불러옵니다.
const { reportData, isLoading } = useWeeklyReport();
const hasReport = computed(() => Boolean(reportData.value && reportData.value.reportId));
const hasAvatarConfig = computed(() => {
  if (!userInfo.value?.avatar) return false;
  return Object.values(userInfo.value.avatar).some((value) => Boolean(value));
});
const goBack = () => {
  router.push({ name: 'user' });
};

const goToReport = () => {
  activeTab.value = 'report';
  router.push({ name: 'report' });
};

const handleWithdraw = async () => {
  const confirmed = await uiStore.openAlert(
    '정말로 회원탈퇴 하시겠습니까?\n이 작업은 되돌릴 수 없습니다.', 
    '탈퇴 확인'
  );
  if (!confirmed) return;

  try {
    await authAPI.withdraw();
    authStore.clearAuth();
    
    await uiStore.openAlert('회원탈퇴가 완료되었습니다.', '알림');
    
    router.push('/guest');
  } catch (error) {
    console.error('회원탈퇴 실패:', error);
    await uiStore.openAlert('회원탈퇴에 실패했습니다.\n잠시 후 다시 시도해주세요.', '오류');
  }
};

const goToAvatarCreate = () => {
  router.push('/avatar/create'); 
};

const displayPureStudyTime = computed(() => {
  // 백엔드 값이 없으면 임시값 사용
  if (!userInfo.value?.dailyPureStudyTime || userInfo.value.dailyPureStudyTime === 0) {
    return authStore.userInfo?.tempPureStudyTime ?? 0;
  }
  return userInfo.value.dailyPureStudyTime;
});
const displayFocusDepth = computed(() => {
  if (!userInfo.value?.dailyFocusDepth || userInfo.value.dailyFocusDepth === 0) {
    return authStore.userInfo?.tempFocusDepth ?? 0;
  }
  return userInfo.value.dailyFocusDepth;
});
</script>

<style scoped>
@media (max-width: 1280px) {
  .mypage-root {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 140px;
    gap: 28px;
  }

  .mypage-menu {
    position: relative;
    left: auto;
    top: auto;
    width: min(92vw, 360px);
    height: auto;
    order: 2;
    margin-bottom: 3rem;
  }

  .mypage-content {
    position: relative;
    left: auto;
    top: auto;
    width: min(95vw, 760px);
    height: auto;
    order: 1;
    margin-top: 109px;
  }

  .mypage-card {
    height: auto !important;
    min-height: 1100px;
  }

  .mypage-menu-buttons {
    position: static;
    margin-top: 16px;
    flex-direction: row;
  }
}
</style>