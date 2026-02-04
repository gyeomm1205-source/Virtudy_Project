<template>
  <nav class="bg-[var(--color-cream2)] h-[75px] absolute top-0 left-0 right-0 flex items-center justify-between px-[78px] py-[28px] overflow-clip z-50">
    <div class="flex items-center gap-[var(--spacing-m)]">
      <router-link 
        :to="logoLink" 
        class="relative z-10 cursor-pointer"
      >
        <LogoIcon class="h-[50px] w-auto" />
      </router-link>
      <router-link 
        to="/introduction" 
        class="text-[var(--color-choco)] text-[24px] font-['PF_Stardust_S'] font-normal leading-none hover:opacity-80 transition-opacity relative z-10 cursor-pointer"
      >
        버터디 소개
      </router-link>
    </div>
    <div class="flex items-center gap-[var(--spacing-m)]">
      <router-link 
        to="/ranking" 
        class="text-[var(--color-choco)] text-[24px] font-['PF_Stardust_S'] font-normal leading-none hover:opacity-80 transition-opacity relative z-10 cursor-pointer"
      >
        랭킹
      </router-link>
      <router-link 
        v-if="isLoggedIn" 
        to="/mypage" 
        class="text-[var(--color-choco)] text-[24px] font-['PF_Stardust_S'] font-normal leading-none hover:opacity-80 transition-opacity relative z-10 cursor-pointer"
      >
        마이페이지
      </router-link>
      <button 
        v-if="!isLoggedIn" 
        @click="kakaoLogin" 
        class="text-[var(--color-choco)] text-[24px] font-['PF_Stardust_S'] font-normal leading-none hover:opacity-80 transition-opacity bg-transparent border-none cursor-pointer relative z-10"
      >
        로그인
      </button>
      <button 
        v-else 
        @click="logout" 
        class="text-[var(--color-choco)] text-[24px] font-['PF_Stardust_S'] font-normal leading-none hover:opacity-80 transition-opacity bg-transparent border-none cursor-pointer relative z-10"
      >
        로그아웃
      </button>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import LogoIcon from '@/assets/logo.svg?component';

const authStore = useAuthStore();
const router = useRouter();

const isLoggedIn = computed(() => authStore.isLoggedIn);
const logoLink = computed(() => (isLoggedIn.value ? '/user' : '/'));

const kakaoLogin = () => {
  const clientId = import.meta.env.VITE_KAKAO_CLIENT_ID;
  const redirectUri = import.meta.env.VITE_KAKAO_REDIRECT_URI;
  const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}`;
  window.location.href = kakaoAuthUrl;
};

const logout = () => {
  authStore.clearAuth();
  router.push('/guest');
};
</script>


