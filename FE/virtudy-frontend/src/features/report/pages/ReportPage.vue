<template>
  <GlobalBackground :skyType="3">
  <div class="relative min-h-[85rem] w-full pb-[8rem] report-root">
    
    <div class="absolute left-[4.75rem] top-[22.25rem] -translate-y-1/2">
      <h1 class="text-[var(--color-pancake)] [text-shadow:4px_4px_0px_var(--color-choco)] text-[9.75rem] font-['Ram'] font-medium leading-none tracking-[-1.17rem] whitespace-nowrap">
        마이<br />페이지
      </h1>
    </div>

    <button 
      @click="goBack"
      class="absolute left-[4.75rem] top-[7.4375rem] w-[4rem] h-[4rem] cursor-pointer hover:scale-110 transition-transform report-back"
    >
      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="var(--color-choco)" class="w-full h-full">
        <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
      </svg>
    </button>

    <div class="absolute left-[calc(8.33%+5.875rem)] top-[16.875rem] w-[15.9375rem] h-[30.25rem] report-menu">
      <div class="absolute top-[16.25rem] left-0 w-full flex flex-col gap-[12px] report-menu-buttons">
        <button 
          @click="goToMyPage"
          class="w-full px-[2rem] py-[1.25rem] border-2 border-[var(--color-choco)] border-solid rounded-[0.125rem] shadow-[4px_4px_0px_0px_var(--color-choco)] cursor-pointer transition-transform hover:scale-105 bg-[var(--color-butter2)]"
        >
          <span class="text-[var(--color-choco)] text-[1.75rem] font-['Xcu'] font-medium leading-none">
            프로필
          </span>
        </button>

        <button 
          class="w-full px-[2rem] py-[1.25rem] border-2 border-[var(--color-choco)] border-solid rounded-[0.125rem] shadow-[4px_4px_0px_0px_var(--color-choco)] cursor-pointer transition-transform hover:scale-105 bg-[var(--color-butter)]"
        >
          <span class="text-[var(--color-choco)] text-[1.75rem] font-['Xcu'] font-medium leading-none">
            리포트
          </span>
        </button>
      </div>
    </div>

    <div class="absolute left-[calc(33.33%+0.3125rem)] top-[6.8125rem] w-[45.75rem] h-[62.5625rem] report-content">
      <div class="bg-[#FFFDF5] border-2 border-[var(--color-choco)] border-solid h-full w-full rounded-[1.25rem] relative shadow-[4px_4px_0px_0px_var(--color-choco)] p-8 box-border report-card flex flex-col overflow-hidden">
        
        <div
          v-if="!hasReport && !isLoading"
          class="absolute inset-0 z-20 flex items-center justify-center rounded-[1.25rem] bg-[rgba(255,253,245,0.75)] backdrop-blur-[2px]"
        >
          <div class="text-center px-10">
            <p class="text-[var(--color-choco)] text-[2rem] font-['Xcu'] leading-snug">
              아직 주간리포트가 없습니다.
            </p>
            <p class="text-[var(--color-choco)] text-[1.5rem] font-['PfStardust30S'] mt-3">
              버터디와 함께 공부를 시작하세요!
            </p>
          </div>
        </div>
        
        <div class="absolute right-[3rem] top-[2rem]" :class="hasReport ? 'z-30' : 'z-10'">
          <button 
            @click="showCalendar = !showCalendar"
            class="hover:scale-105 transition-transform flex items-center gap-[0.178rem] bg-[#FFF2CC] px-[0.948rem] py-[0.474rem] rounded-[1.875rem]"
            style="color: #805143; font-family: 'PfStardust30S'; font-size: 1.25rem;"
          >
            <svg width="23" height="23" viewBox="0 0 23 23" fill="none" xmlns="http://www.w3.org/2000/svg" class="shrink-0">
              <path d="M6.375 2.5V5.25M16.625 2.5V5.25M3.0625 8H19.9375M5.25 3.9375H17.75C18.8546 3.9375 19.75 4.83289 19.75 5.9375V18.4375C19.75 19.5421 18.8546 20.4375 17.75 20.4375H5.25C4.14543 20.4375 3.25 19.5421 3.25 18.4375V5.9375C3.25 4.83289 4.14543 3.9375 5.25 3.9375Z" stroke="#805143" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            {{ formatDateRange(currentWeek.monday, currentWeek.sunday) }}
            <div class="relative shrink-0 w-[2rem] h-[1.25rem]">
              <div class="absolute left-[0.125rem] top-[0.125rem] w-[1.75rem] h-[1rem]">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="10" viewBox="0 0 18 10" fill="none" class="block max-w-none size-full">
                  <path d="M18 0V2H16V4H14L14 6H12V8H10V10H8V8L6 8L6 6L4 6V4L2 4L2 2H0L0 0L18 0Z" :fill="'var(--color-syrup)'" :stroke="'var(--color-choco)'" stroke-width="1.2"/>
                </svg>
              </div>
            </div>
          </button>

          <div v-if="showCalendar" class="absolute top-10 right-0 shadow-xl rounded-[20px] z-40">
            <WeeklyCalendar 
              :selectedDate="baseDate"
              @select-date="handleDateSelect"
              @close="showCalendar = false"
            />
          </div>
        </div>

        <h2 class="text-[var(--color-choco)] text-[2.5rem] font-['Ram'] mt-4 mb-6 shrink-0">주간리포트</h2>

        <div class="flex gap-6 mb-6 px-4 shrink-0">
          <div class="flex-1 bg-[var(--color-choco)] rounded-[20px] px-6 py-5 text-center text-[var(--color-cream)] shadow-md flex flex-col justify-center">
            <p class="text-[2.25rem] font-['PfStardust30S'] mb-2 leading-tight">총 공부시간</p>
            <p class="text-[3rem] font-bold font-['Ram'] leading-none">
              {{ formatStudyTime(reportData?.totalStudyTime) }}
            </p>
          </div>
          
          <div class="flex-1 bg-[var(--color-choco)] rounded-[20px] px-6 py-5 text-center text-[var(--color-cream)] shadow-md flex flex-col justify-center">
            <p class="text-[2.25rem] font-['PfStardust30S'] mb-2 leading-tight">평균집중도</p>
            <p class="text-[3rem] font-bold font-['Ram'] leading-none">
              {{ reportData?.focusDepthPercentage || 0 }}%
            </p>
          </div>
        </div>

        <div class="mb-6 px-4 shrink-0"> 
            <h3 class="w-full text-left text-[2rem] text-[var(--color-choco)] font-['Xcu'] mb-2">주간성취</h3>
            
<PentagonChart 
              class="w-full"
              :endurance="reportData?.endurance || 0"
              :focusDepth="reportData?.focusDepth || 0"
              :regularity="reportData?.regularity || 0"
              :stability="reportData?.stability || 0"
              :willPower="reportData?.willPower || 0"
              :layout="{
                /* [리포트 페이지 레이아웃 전략]
                   마이페이지와 동일한 로직을 적용하되, 리포트 페이지의 너비에 맞게 미세 조정합니다.
                   1. 모바일(기본): 글씨 작게(1.1rem), 위아래/양옆으로 바짝 붙임(top-25%, right-0)
                   2. 데스크탑(xl): 글씨 크게(2rem), 여유 있게 배치
                */

                // 지구력
                endurance: 'top-[-1%] left-1/2 -translate-x-1/2 text-[1.5rem] xl:text-[2rem] xl:top-[-4%]',
                
                // 집중력
                focusDepth: 'top-[32%] right-[-5%] sm:right-[15%] text-[1.5rem] xl:right-20 xl:text-[2rem] xl:top-[30%]',
                
                // 규칙성
                regularity: 'bottom-5 right-7 sm:right-[22%] text-[1.5rem] xl:bottom-[1.5%] xl:right-34 xl:text-[2rem]',
                
                // 안정감
                stability: 'bottom-5 left-7 sm:left-[22%] text-[1.5rem] xl:bottom-[1.5%] xl:left-34 xl:text-[2rem]',
                
                // 의지력
                willPower: 'top-[32%] left-[-5%] sm:left-[15%] text-[1.5rem] xl:left-20 xl:text-[2rem] xl:top-[30%]'
              }"
            />
          </div>

        <div class="px-4 pb-2 flex-1 min-h-0 flex flex-col">
          <h3 class="text-[2rem] text-[var(--color-choco)] font-['Xcu'] mb-4 shrink-0">AI 코멘트</h3>
          
          <div class="bg-[var(--color-cream)] rounded-[20px] p-6 relative flex-1 w-full h-full overflow-y-auto custom-scroll">
            <p 
              class="text-[var(--color-choco)] font-['PfStardust30S'] text-[1.25rem] leading-relaxed pr-[1rem] whitespace-pre-wrap ai-comment-text"
              v-html="formattedAiComment"
            ></p>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="showNoReportModal" class="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div class="bg-[var(--color-cream)] border-2 border-[var(--color-choco)] rounded-[20px] p-8 shadow-[4px_4px_0px_0px_var(--color-choco)] flex flex-col items-center gap-4 animate-bounce-in min-w-[300px]">
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--color-choco)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="8" x2="12" y2="12"></line>
          <line x1="12" y1="16" x2="12.01" y2="16"></line>
        </svg>
        <p class="text-[var(--color-choco)] font-['PfStardust30S'] text-xl text-center whitespace-pre-line leading-relaxed">
          해당 주에는<br/>주간 리포트가 없습니다.
        </p>
      </div>
    </div>

    <div class="absolute bottom-0 left-0 w-full z-50">
      <GlobalFooter />
    </div>
  </div>
  </GlobalBackground>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useWeeklyReport } from '../logic/useWeeklyReport';

import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import PentagonChart from '@/shared/ui/PentagonChart.vue';
import WeeklyCalendar from '../pages/WeeklyCalendar.vue';
import { useAuthStore } from '@/stores/authStore';
import GlobalBackground from '@/shared/ui/GlobalBackground.vue';

const router = useRouter();
const showCalendar = ref(false);
const authStore = useAuthStore();

const showNoReportModal = ref(false);
const lastValidDate = ref<Date>(new Date()); 

const { 
  reportData, 
  isLoading,
  currentWeek, 
  baseDate, 
  changeWeek: originalChangeWeek,
} = useWeeklyReport();

const hasReport = computed(() => Boolean(reportData.value && reportData.value.reportId));
const hasAvatarConfig = computed(() => {
  if (!authStore.userInfo?.avatar) return false;
  return Object.values(authStore.userInfo.avatar).some((value) => Boolean(value));
});

// [추가] AI 코멘트 하이라이팅을 위한 Computed
const formattedAiComment = computed(() => {
  const comment = reportData.value?.aiComment || "열심히 공부한 당신에게 멋진 분석을 준비 중이에요...";
  
  // 1. 숫자+점+공백 패턴 (예: "1. ", "2. ") 뒤에 오는 키워드(단어)를 찾아서 굵게 표시
  // 정규식 설명: (\d+\.\s+)([^:]+:) -> "숫자. 공백" 그룹1, "콜론 앞까지의 단어:" 그룹2
  // 예: "1. 진단:" -> "<b>1. 진단:</b>" 으로 변환
  // 또는 단순히 "1. 진단", "2. 비용" 같은 패턴을 찾고 싶다면 아래와 같이 수정
  
  // 여기서는 "숫자. 단어:" 형태를 찾아 굵게 만듭니다.
  let formatted = comment.replace(/(\d+\.\s+[^:]+:)/g, '<b class="font-bold text-[1.35rem]">$1</b>');
  
  // 만약 콜론(:)이 없는 경우 "숫자. 단어" 형태를 찾으려면:
  // formatted = comment.replace(/(\d+\.\s+\S+)/g, '<b>$1</b>');

  return formatted;
});

const handleDateSelect = (date: Date) => {
  showCalendar.value = false; 
  originalChangeWeek(date);

  const unwatch = watch(isLoading, (newLoading, oldLoading) => {
    if (!newLoading && oldLoading) {
      unwatch(); 
      if (!reportData.value || !reportData.value.reportId) {
        showNoReportModal.value = true;
        setTimeout(() => {
          showNoReportModal.value = false;
          originalChangeWeek(lastValidDate.value);
        }, 1500);
      } else {
        lastValidDate.value = new Date(date);
      }
    }
  });
};

onMounted(() => {
  if (authStore.isLoggedIn && !authStore.userInfo) {
    authStore.fetchUserInfo();
  }
  lastValidDate.value = new Date(baseDate.value);
});

const formatDateRange = (startDate: Date, endDate: Date) => {
  const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
  const startDay = startDate.getDate();
  const endDay = endDate.getDate();
  const month = monthNames[startDate.getMonth()];
  const year = startDate.getFullYear();
  return `${startDay} - ${endDay} ${month} ${year}`;
};

const formatStudyTime = (minutes: number | undefined) => {
  if (!minutes) return '0h 0m';
  const h = Math.floor(minutes / 60); 
  const m = minutes % 60; 
  if (m === 0) return `${h}h`;
  return `${h}h ${m}m`;
};

const goBack = () => router.push({ name: 'user' });
const goToMyPage = () => router.push({ name: 'mypage' });
</script>

<style scoped>
.custom-scroll::-webkit-scrollbar {
  width: 6px; 
}
.custom-scroll::-webkit-scrollbar-track {
  background: transparent; 
}

.custom-scroll::-webkit-scrollbar-thumb {
  background-color: var(--color-choco);
  border-radius: 10px;
  border: 2px solid var(--color-cream); /* 얇아 보이게 테두리 두께 조정 */
  background-clip: content-box;
}

.custom-scroll::-webkit-scrollbar-thumb:hover {
  background-color: #6b4438;
}

/* 위쪽 화살표 */
.custom-scroll::-webkit-scrollbar-button:vertical:start:decrement {
  display: block;
  border-top-left-radius: 20px;
  border-top-right-radius: 20px;
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
}

/* 아래쪽 화살표 */
.custom-scroll::-webkit-scrollbar-button:vertical:end:increment {
  display: block;
  background-repeat: no-repeat;
  background-position: center;
  background-size: 12px;
}

.custom-scroll::-webkit-scrollbar-button:hover {
  background-color: rgba(128, 81, 67, 0.1);
  border-radius: 50%;
}

/* v-html로 주입된 b 태그 스타일 적용을 위해 (scoped에서 깊은 선택자 필요할 수 있음) */
:deep(.ai-comment-text b) {
  font-weight: 900; /* 아주 굵게 */
  color: #5c3a30; /* 약간 더 진한 초코색 */
}

/* 애니메이션 */
@keyframes bounceIn {
  0% { transform: scale(0.8); opacity: 0; }
  60% { transform: scale(1.05); opacity: 1; }
  100% { transform: scale(1); }
}
.animate-bounce-in {
  animation: bounceIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@media (max-width: 1280px) {
  .report-root {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 140px;
    gap: 28px;
  }

  .report-menu {
    position: relative;
    left: auto;
    top: auto;
    width: min(92vw, 360px);
    height: auto;
    order: 2;
    margin-bottom: 3rem;
  }

  .report-content {
    position: relative;
    left: auto;
    top: auto;
    width: min(95vw, 760px);
    height: auto;
    order: 1;
    margin-top: 109px;
  }

  .report-menu-buttons {
    position: static;
    margin-top: 16px;
    flex-direction: row;
  }

  .report-card {
    height: auto !important;
    min-height: 1100px;
  }
}
</style>