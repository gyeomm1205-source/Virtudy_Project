<template>
  <div 
    class="relative w-full h-screen bg-cover bg-center bg-no-repeat"
    :style="{ backgroundImage: `url(${backgroundImage})` }"
  >
    <!-- Navigation -->
    <GlobalNavBar />
    
    <!-- Main Content -->
    <div class="absolute flex flex-col gap-[80px] h-[419px] items-center justify-center left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[844px]">
      <!-- Page Title -->
      <div class="flex flex-col gap-[24px] items-center justify-center text-center w-full">
        <img :src="logoImage" alt="아바타 캠스터디" class="h-[90%]" />
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
  <!-- <GlobalFooter class="absolute bottom-0 w-full left-0" /> -->
</template>

<script setup lang="ts">
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
// import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import backgroundImage from '@/assets/room/bg_photo_1.png';
import logoImage from '@/assets/logo.svg?url';

const kakaoLogin = () => {
  const clientId = import.meta.env.VITE_KAKAO_CLIENT_ID;
  const redirectUri = import.meta.env.VITE_KAKAO_REDIRECT_URI;
  const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}`;
  
  // 카카오 인증 페이지로 리다이렉트
  window.location.href = kakaoAuthUrl;
};
</script>