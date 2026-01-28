<template>
  <div class="bg-[var(--color-cream)] relative min-h-[85rem] w-full pb-[8rem]">
    <!-- Global Navigation -->
    <GlobalNavBar />
    
    <!-- 마이페이지 제목 -->
    <div class="absolute left-[4.75rem] top-[22.25rem] -translate-y-1/2">
      <h1 class="text-[var(--color-pancake)] text-[9.75rem] font-['Ram'] font-medium leading-none tracking-[-1.17rem] whitespace-nowrap">
        마이<br />페이지
      </h1>
    </div>

    <!-- 뒤로가기 버튼 -->
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

    <!-- 왼쪽 프로필과 메뉴 -->
    <div class="absolute left-[calc(8.33%+5.875rem)] top-[16.875rem] w-[15.9375rem] h-[30.25rem]">
      <!-- 메뉴 버튼들 -->
      <div class="absolute top-[16.25rem] left-0 w-full flex flex-col gap-[0.625rem]">
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

    <!-- 오른쪽 메인 콘텐츠 -->
    <div class="absolute left-[calc(33.33%+0.3125rem)] top-[6.8125rem] w-[45.75rem] h-[62.5625rem]">
      <div class="bg-[var(--color-syrup)] border-2 border-[var(--color-choco)] border-solid h-full w-full rounded-[1.25rem] overflow-hidden relative shadow-[4px_4px_0px_0px_var(--color-choco)]">
        <!-- 회원정보수정 버튼 -->
        <button 
          @click="openEditModal"
          class="absolute right-[3rem] top-[1.1875rem] bg-[var(--color-cream2)] border-2 border-[var(--color-choco)] border-solid px-[2.2rem] py-[0.8rem] rounded-[1.875rem] shadow-[4px_4px_0px_0px_var(--color-choco)] cursor-pointer hover:scale-105 transition-transform"
        >
          <span class="text-[var(--color-choco)] text-[1.5rem] font-['PfStardust30S'] font-normal leading-none">
            회원정보수정
          </span>
        </button>
        <!-- 프로필 영역 -->
        <div class="absolute left-[18.125rem] top-[4.75rem] flex flex-col items-center">
          <!-- 아바타 -->
          <div class="relative w-[9.125rem] h-[9.125rem] mb-[1.5rem]">
            <div class="w-[9.125rem] h-[9.125rem] rounded-full overflow-hidden border-4 border-[var(--color-choco)]">
              <img 
                v-if="userInfo?.avatarImageUrl" 
                :src="userInfo.avatarImageUrl" 
                alt="프로필 사진"
                class="w-full h-full object-cover"
              />
              <div v-else class="w-full h-full bg-[var(--color-butter)] flex items-center justify-center text-[2rem] font-bold text-[var(--color-choco)]">
                ME
              </div>
            </div>
          </div>
          
          <!-- 닉네임 -->
          <h2 class="text-[var(--color-choco)] text-[1.75rem] font-['Xcu'] font-medium leading-none mb-[1.1rem]">
            {{ userInfo?.nickName || '닉네임' }}
          </h2>
          
          <!-- 점수와 티어 -->
          <div class="flex items-center gap-[1rem] mb-[0.25rem]">
            <span class="text-[var(--color-pancake)] text-[1.5rem] font-['PfStardust30S'] font-normal leading-none">
              {{ userInfo?.tierScore || 0 }}p
            </span>
            <span class="text-[var(--color-pancake)] text-[1.5rem] font-['PfStardust30S'] font-normal leading-none">
              {{ userInfo?.tier || '티어명' }}
            </span>
          </div>
          
          <!-- 최애 스터디 -->
          <p class="text-[var(--color-choco)] text-[1.5rem] font-['PfStardust30S'] font-normal leading-none">
            &lt;{{ userInfo?.favoriteRoomTitle || '최애스터디이름' }}&gt;
          </p>
        </div>

        <!-- 미니 리포트 섹션 -->
        <div class="absolute left-0 top-[22.625rem] w-full px-[1.25rem]">
          <!-- 미니 리포트 제목 -->
          <div class="flex items-end justify-between mb-[0.5rem]">
            <h3 class="text-[var(--color-butter)] text-[2.625rem] font-['Ram'] font-medium leading-[3rem] tracking-[-0.0525rem] ml-[1rem]">
            미니 리포트
          </h3>

          <button class="text-[var(--color-cream2)] text-[1.25rem] font-['PfStardust30S'] font-normal leading-none tracking-[-0.05rem] underline cursor-pointer mr-[1rem]">
            리포트 상세보기
          </button>
          </div>
    
          <!-- 공부시간/집중도 카드 -->
          <div class="mb-[1.25rem]">
            <MiniReport 
              :studyTime="userInfo?.dailyPureStudyTime" 
              :focusing="userInfo?.dailyFocusDepth" 
            />
          </div>
          
          <!-- 오각형 그래프 -->
          <div class="mb-[1.25rem]">
            <PentagonChart 
            :endurance="reportData?.endurance || 0"
            :focusDepth="reportData?.focusDepth || 0"
            :regularity="reportData?.regularity || 0"
            :stability="reportData?.stability || 0"
            :willPower="reportData?.willPower || 0"
          />
          </div>
        </div>
        
        <!-- 회원탈퇴 링크 -->
        <button class="absolute right-[2.5rem] bottom-[2rem] text-[var(--color-syrup)] text-[0.75rem] font-['Pretendard'] font-normal leading-none tracking-[-0.03rem] underline cursor-pointer">
          회원탈퇴
        </button>
      </div>
    </div>

    <!-- Global Footer -->
    <GlobalFooter class="absolute bottom-0 w-full left-0" />
    
    <!-- 프로필 수정 모달 -->
    <ProfileEditModal 
      v-if="isEditModalOpen"
      :email="userInfo?.email || ''"
      v-model:nickName="editForm.nickName"
      v-model:jobType="editForm.jobType"
      :jobOptions="JOB_OPTIONS"
      @close="closeEditModal"
      @submit="submitEdit"
    />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useMyPage } from '../logic/useMyPage';
import { JOB_OPTIONS } from '../types/mypage.types';
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import MiniReport from '@/shared/ui/MiniReport.vue';
import PentagonChart from '@/shared/ui/PentagonChart.vue';  
import ProfileEditModal from '../ui/ProfileEditModal.vue';
import { useWeeklyReport } from  '@/features/report/logic/useWeeklyReport';

const router = useRouter();
const { 
  userInfo, activeTab, isEditModalOpen, editForm, 
  openEditModal, closeEditModal, submitEdit 
} = useMyPage();

// useWeeklyReport 내부의 onMounted가 실행되면서 자동으로 '지난주' 데이터를 불러옵니다.
const { reportData } = useWeeklyReport();
const goBack = () => {
  router.push({ name: 'user' });
};

const goToReport = () => {
  activeTab.value = 'report';
  router.push({ name: 'report' });
};
</script>

