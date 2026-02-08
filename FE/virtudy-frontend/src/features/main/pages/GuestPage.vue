<template>
  <GlobalBackground :skyType="1" type="village">
  <div>
    
    <!-- Main Content -->
    <div class="absolute flex flex-col gap-[80px] h-[419px] items-center justify-center left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[844px]">
      <!-- Page Title -->
      <div class="flex flex-col gap-[24px] items-center justify-center text-center w-full">
        <img :src="logoImage" alt="아바타 캠스터디" class="h-[90%] animate-bounce-custom" />
      </div>
      
      <!-- Login Button -->
      <button 
        @click="kakaoLogin"
        class="butter-btn"
      >
        <span class="butter-btn-text">
          카카오로 시작하기
        </span>
      </button>
    </div>
  </div>
  </GlobalBackground>
</template>

<script setup lang="ts">
import logoImage from '@/assets/logo.svg?url';
import GlobalBackground from '@/shared/ui/GlobalBackground.vue';

const kakaoLogin = () => {
  const clientId = import.meta.env.VITE_KAKAO_CLIENT_ID;
  const redirectUri = import.meta.env.VITE_KAKAO_REDIRECT_URI;
  const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}`;
  
  // 카카오 인증 페이지로 리다이렉트
  window.location.href = kakaoAuthUrl;
};
</script>

<style scoped>
/* 통통 튀는 애니메이션 정의 */
@keyframes tongtong {
  0%, 100% {
    transform: translateY(5px); /* 원래 위치 */
  }
  50% {
    transform: translateY(-5px); /* 위로 5px 올라감 */
  }
}

.animate-bounce-custom {
  /* 애니메이션 이름 / 재생시간 / 반복여부 / 속도곡선 */
  animation: tongtong 1.5s infinite ease-in-out;
  
  /* 이미지가 렌더링될 때 부드럽게 보이도록 설정 */
  will-change: transform;
}
</style>
