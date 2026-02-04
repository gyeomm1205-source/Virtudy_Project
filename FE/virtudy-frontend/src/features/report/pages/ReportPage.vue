<template>
  <div class="bg-[var(--color-cream)] relative min-h-[85rem] w-full pb-[8rem]">
    <GlobalNavBar />
    
    <div class="absolute left-[4.75rem] top-[22.25rem] -translate-y-1/2">
      <h1 class="text-[var(--color-pancake)] text-[9.75rem] font-['Ram'] font-medium leading-none tracking-[-1.17rem] whitespace-nowrap">
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

    <div class="absolute left-[calc(8.33%+5.875rem)] top-[16.875rem] w-[15.9375rem] h-[30.25rem]">
      <div class="absolute top-[0.5rem] left-0 w-full flex justify-center">
        <div class="w-[9.125rem] h-[9.125rem] rounded-full overflow-hidden border-4 border-[var(--color-choco)] bg-[var(--color-cream)]">
          <CharacterAvatar
            v-if="hasAvatarConfig"
            :config="authStore.userInfo!.avatar!"
            class="w-full h-full"
          />
          <img
            v-else-if="authStore.userInfo?.avatarImageUrl"
            :src="authStore.userInfo.avatarImageUrl"
            alt="프로필"
            class="w-full h-full object-cover"
          />
          <div v-else class="w-full h-full flex items-center justify-center text-[var(--color-choco)] font-bold text-xl">
            ME
          </div>
        </div>
      </div>

      <div class="absolute top-[16.25rem] left-0 w-full flex flex-col gap-[0.625rem]">
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

    <div class="absolute left-[calc(33.33%+0.3125rem)] top-[6.8125rem] w-[45.75rem] h-[62.5625rem]">
      <div class="bg-[#FFFDF5] border-2 border-[var(--color-choco)] border-solid h-full w-full rounded-[1.25rem] relative shadow-[4px_4px_0px_0px_var(--color-choco)] p-8 box-border">
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
            style="
              color: #805143;
              font-family: 'PfStardust30S';
              font-size: 1.25rem;
              font-style: normal;
              font-weight: 400;
              line-height: normal;
              letter-spacing: -0.05rem;
            "
          >
            <svg 
              width="23" 
              height="23" 
              viewBox="0 0 23 23" 
              fill="none" 
              xmlns="http://www.w3.org/2000/svg"
              class="shrink-0"
            >
              <path 
                d="M6.375 2.5V5.25M16.625 2.5V5.25M3.0625 8H19.9375M5.25 3.9375H17.75C18.8546 3.9375 19.75 4.83289 19.75 5.9375V18.4375C19.75 19.5421 18.8546 20.4375 17.75 20.4375H5.25C4.14543 20.4375 3.25 19.5421 3.25 18.4375V5.9375C3.25 4.83289 4.14543 3.9375 5.25 3.9375Z" 
                stroke="#805143" 
                stroke-width="1.5" 
                stroke-linecap="round" 
                stroke-linejoin="round"
              />
            </svg>
            {{ formatDateRange(currentWeek.monday, currentWeek.sunday) }}
            <div class="relative shrink-0 w-[2rem] h-[1.25rem]">
              <div class="absolute left-[0.125rem] top-[0.125rem] w-[1.75rem] h-[1rem]">
                <img 
                  alt="" 
                  class="block max-w-none size-full" 
                  src="http://localhost:3845/assets/fc82f790716939d8d7fb00be557667ae980832c8.svg" 
                />
              </div>
            </div>
          </button>

          <div v-if="showCalendar" class="absolute top-10 right-0 shadow-xl rounded-[20px] z-40">
             <WeeklyCalendar 
              :selectedDate="baseDate"
              @select-date="changeWeek"
              @close="showCalendar = false"
            />
          </div>
        </div>

        <h2 class="text-[var(--color-choco)] text-[2.5rem] font-['Ram'] mt-4 mb-6">주간리포트</h2>

        <div class="flex gap-6 mb-6 px-4">
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

        <div class="mb-6 px-4"> 
          <h3 class="w-full text-left text-[2rem] text-[var(--color-choco)] font-['Xcu'] mb-2">주간성취</h3>
          <PentagonChart 
            class="w-full"
            :endurance="reportData?.endurance || 0"
            :focusDepth="reportData?.focusDepth || 0"
            :regularity="reportData?.regularity || 0"
            :stability="reportData?.stability || 0"
            :willPower="reportData?.willPower || 0"
          />
        </div>

        <div class="px-4 pb-4">
          <h3 class="text-[2rem] text-[var(--color-choco)] font-['Xcu'] mb-4">AI 코멘트</h3>
          <div class="bg-[var(--color-cream)] border-2 border-[var(--color-syrup)] rounded-[20px] p-6 relative min-h-[8rem]">
            <p class="text-[var(--color-choco)] font-['PfStardust30S'] text-[1.25rem] leading-relaxed pr-[4rem]">
              {{ reportData?.aiComment || "열심히 공부한 당신에게 멋진 분석을 준비 중이에요..." }}
            </p>
            <img 
              src="http://localhost:3845/assets/ae7ca0939b29738c16aee5cf86953e893d60c594.svg" 
              class="absolute right-4 bottom-2 w-[4rem] h-[4rem]"
              alt="AI Character"
            />
          </div>
        </div>

      </div>
    </div>

    <GlobalFooter class="absolute bottom-0 w-full left-0" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useWeeklyReport } from '../logic/useWeeklyReport';

import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import PentagonChart from '@/shared/ui/PentagonChart.vue';
import WeeklyCalendar from '../pages/WeeklyCalendar.vue';
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const showCalendar = ref(false);
const authStore = useAuthStore();

const { 
  reportData, 
  isLoading,
  currentWeek, 
  baseDate, 
  changeWeek,
} = useWeeklyReport();

const hasReport = computed(() => Boolean(reportData.value && reportData.value.reportId));
const hasAvatarConfig = computed(() => {
  if (!authStore.userInfo?.avatar) return false;
  return Object.values(authStore.userInfo.avatar).some((value) => Boolean(value));
});

onMounted(() => {
  if (authStore.isLoggedIn && !authStore.userInfo) {
    authStore.fetchUserInfo();
  }
});

const formatDateRange = (startDate: Date, endDate: Date) => {
  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];
  
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
  //분이 딱 맞아떨어지면 시간만 표시(120분->2h)
  if (m === 0) return `${h}h`;
  //나머지가 있으면 분까지 리턴(135분->2h 15m)           
  return `${h}h ${m}m`;
};

const goBack = () => {
  router.push({ name: 'user' });
};

const goToMyPage = () => {
  router.push({ name: 'mypage' });
};
</script>