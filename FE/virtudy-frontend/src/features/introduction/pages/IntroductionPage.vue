<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import bgBrick from '@/assets/bg_brick.png';

const router = useRouter();
const authStore = useAuthStore();

const startTutorial = () => {
  // name: 'tutorial'로 이동
  router.push({ name: 'tutorial' });
};

const kakaoLogin = () => {
  const clientId = import.meta.env.VITE_KAKAO_CLIENT_ID;
  const redirectUri = import.meta.env.VITE_KAKAO_REDIRECT_URI;
  const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}`;
  
  // 카카오 인증 페이지로 리다이렉트
  window.location.href = kakaoAuthUrl;
};

const handleButtonClick = () => {
  if (authStore.isLoggedIn) {
    startTutorial();
  } else {
    kakaoLogin();
  }
};
</script>

<template>
  <div 
    class="min-h-screen"
    :style="{ 
      backgroundImage: `url(${bgBrick})`,
      backgroundRepeat: 'repeat',
      backgroundSize: '500px'
    }"
  >
    <div class="flex flex-col items-center justify-center min-h-screen py-[4rem] px-[1rem]">
      
      <div class="max-w-[46rem] w-full mx-[1.25rem] sm:mx-[2rem] bg-white border-2 border-[var(--color-choco)] rounded-[0.75rem] p-[2rem] sm:p-[3rem] shadow-[6px_6px_0px_0px_var(--color-choco)] text-center">
        
        <div class="mb-[2rem]">
          <h1 class="text-[var(--color-syrup)] text-[3.5rem] sm:text-[4.5rem] font-['Ram'] leading-tight tracking-[-0.0525rem] mb-2">
            Virtudy
          </h1>
          <p class="text-[var(--color-choco)] text-[1.8rem] sm:text-[2.2rem] font-['PfStardust30S'] font-bold">
            버터디에 어서오세요!
          </p>
        </div>

        <div class="flex flex-col gap-[1.5rem] text-[var(--color-choco)] font-['PfStardust30S'] text-[1.1rem] sm:text-[1.25rem] leading-relaxed break-keep">
          
          <p>
            Virtudy는 캠 화면 대신<br class="sm:hidden" />
            <span class="text-[var(--color-syrup)] text-[1.4rem] sm:text-[1.6rem] font-bold mx-1">아바타로 함께 공부하는</span><br class="sm:hidden" />
            실시간 스터디 플랫폼입니다.
          </p>

          <p>
            버터 마을을 배경으로 한 스터디룸에서<br />
            친구들과 공부해 보세요!
          </p>

          <p>
            자리를 비우거나, 졸거나, 폰을 사용하면<br />
            아바타가 내 모습을 따라합니다.
          </p>

          <p>
            같은 스터디룸에서 딴짓하는 친구에게는<br />
            <span class="inline-block bg-[var(--color-cream2)] border border-[var(--color-choco)] rounded px-2 py-0.5 mx-1 text-[0.9em]">&lt;깨우기&gt;</span> 버튼으로 
            <span class="text-[var(--color-jam)] text-[1.5rem] font-bold mx-1 drop-shadow-[2px_2px_0px_rgba(0,0,0,0.1)]">섬광탄</span>을<br class="sm:hidden" />
            던질 수 있어요.<br />
            <span class="text-[0.9rem] opacity-70">(물론 나도 맞을 수 있으니 주의하세요!)</span>
          </p>

          <div class="bg-[var(--color-cream)] border border-[var(--color-choco)] rounded-[1rem] p-6 mt-2 shadow-[4px_4px_0px_0px_rgba(128,81,67,0.2)]">
            <p class="mb-2">일정 시간 이상 공부하지 않으면<br />버터 마을에 운석이 떨어집니다.</p>
            <p class="text-[var(--color-syrup)] text-[1.3rem] sm:text-[1.5rem] font-bold">
              친구에게 섬광탄을 던져 깨우고,<br />
              버터 마을을 지켜주세요!
            </p>
          </div>

        </div>

        <div class="mt-[3rem]">
          <button 
            @click="handleButtonClick"
            class="group relative inline-flex items-center justify-center px-[2rem] py-[1rem] bg-[var(--color-butter)] border-2 border-[var(--color-choco)] rounded-[0.5rem] shadow-[4px_4px_0px_0px_var(--color-choco)] active:shadow-[2px_2px_0px_0px_var(--color-choco)] active:translate-y-[2px] transition-all cursor-pointer hover:bg-[#ffeb99]"
          >
            <span class="text-[var(--color-choco)] font-['PfStardust30S'] text-[1.4rem] font-bold group-hover:scale-105 transition-transform">
              {{ authStore.isLoggedIn ? '튜토리얼 시작하기' : '카카오로 시작하기' }}
            </span>
            <span class="absolute -top-2 -right-2 flex h-4 w-4">
              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-[var(--color-syrup)] opacity-75"></span>
              <span class="relative inline-flex rounded-full h-4 w-4 bg-[var(--color-syrup)]"></span>
            </span>
          </button>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
/* 필요한 경우 추가 스타일 작성 */
</style>
