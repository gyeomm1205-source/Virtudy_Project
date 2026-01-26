<template>
  <nav class="bg-[var(--color-cream2)] h-[75px] absolute top-0 left-0 right-0 flex items-center justify-between px-[78px] py-[28px] overflow-clip">
    <div class="flex items-center gap-[var(--spacing-m)]">
      <router-link 
        :to="logoLink" 
        class="text-[var(--color-syrup)] text-[36px] font-['Ram'] font-medium leading-none"
      >
        버터디
      </router-link>
      <router-link 
        to="/introduction" 
        class="text-[var(--color-choco)] text-[24px] font-['PF_Stardust_S'] font-normal leading-none hover:opacity-80 transition-opacity"
      >
        버터디 소개
      </router-link>
    </div>
    <div class="flex items-center gap-[var(--spacing-m)]">
      <router-link 
        to="/ranking" 
        class="text-[var(--color-choco)] text-[24px] font-['PF_Stardust_S'] font-normal leading-none hover:opacity-80 transition-opacity"
      >
        랭킹
      </router-link>
      <router-link 
        v-if="isLoggedIn" 
        to="/mypage" 
        class="text-[var(--color-choco)] text-[24px] font-['PF_Stardust_S'] font-normal leading-none hover:opacity-80 transition-opacity"
      >
        마이페이지
      </router-link>
      <button 
        v-if="!isLoggedIn" 
        @click="kakaoLogin" 
        class="text-[var(--color-choco)] text-[24px] font-['PF_Stardust_S'] font-normal leading-none hover:opacity-80 transition-opacity bg-transparent border-none cursor-pointer"
      >
        로그인
      </button>
      <button 
        v-else 
        @click="logout" 
        class="text-[var(--color-choco)] text-[24px] font-['PF_Stardust_S'] font-normal leading-none hover:opacity-80 transition-opacity bg-transparent border-none cursor-pointer"
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
  router.push('/');
};
</script>


