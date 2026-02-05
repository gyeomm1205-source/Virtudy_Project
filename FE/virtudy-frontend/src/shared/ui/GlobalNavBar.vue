<template>
  <!-- 모바일 메뉴 버튼 (완전 분리) -->
<button
  @click="toggleMenu"
  class="xl:hidden fixed top-6 right-6 z-[999] w-10 h-10 flex items-center justify-center bg-transparent border-none"
  :aria-label="isMenuOpen ? '메뉴 닫기' : '메뉴 열기'"
>
  <!-- X -->
  <svg
    v-if="isMenuOpen"
    width="40"
    height="40"
    viewBox="0 0 32 32"
    fill="none"
  >
    <line x1="8" y1="8" x2="24" y2="24" stroke="#805143" stroke-width="2.5" stroke-linecap="round"/>
    <line x1="24" y1="8" x2="8" y2="24" stroke="#805143" stroke-width="2.5" stroke-linecap="round"/>
  </svg>

  <!-- 햄버거 -->
  <div v-else class="flex flex-col gap-1.5">
    <div class="w-8 h-1 bg-[var(--color-choco)] rounded-full"></div>
    <div class="w-8 h-1 bg-[var(--color-choco)] rounded-full"></div>
    <div class="w-8 h-1 bg-[var(--color-choco)] rounded-full"></div>
  </div>
</button>

  <nav class="h-[75px] absolute top-3 left-0 right-0 flex items-center justify-between px-[20px] xl:px-[78px] py-[28px] z-50 navbar">
    
    <div class="flex items-center gap-[var(--spacing-m)] navbar-links">
      <router-link 
        :to="logoLink" 
        class="relative z-20 cursor-pointer"
      >
        <LogoIcon class="h-[50px] xl:h-[62px] w-auto" />
      </router-link>

      <router-link 
        to="/introduction" 
        class="hidden xl:flex group intro-btn relative items-center justify-center cursor-pointer hover:opacity-80 transition-all duration-100 ease-in-out w-[9rem] h-[2.7rem] text-[#FFD966] mt-[4px] active:translate-x-[4px] active:translate-y-[4px] text-[24px] font-['PfStardust30S'] font-normal leading-none"
        style="--btn-highlight: #FFEAAC;"
      >
        <IntroButtonBg class="absolute inset-0 w-full h-full drop-shadow-[4px_4px_0_#fff2cc] transition-all duration-100 group-active:drop-shadow-none" />
        <span class="relative z-10 pt-0.9 text-[var(--color-choco)]">
          버터디 소개
        </span>
      </router-link>
    </div>

    <div class="hidden xl:flex items-center gap-[var(--spacing-m)] navbar-links">
      
      
      <router-link 
      v-if="isLoggedIn"
      to="/ranking" 
      class="group intro-btn relative flex items-center justify-center cursor-pointer hover:opacity-80 transition-all duration-100 ease-in-out w-[6.5rem] h-[2.7rem] text-[#FFC954] mt-[4px] active:translate-x-[4px] active:translate-y-[4px] text-[24px] font-['PfStardust30S'] font-normal leading-none"
      style="--btn-highlight: #FFD966;"
      >
      <IntroButtonBg class="absolute inset-0 w-full h-full drop-shadow-[4px_4px_0_#fff2cc] transition-all duration-100 group-active:drop-shadow-none" />
      <span class="relative z-10 pt-0.9 text-[var(--color-choco)]">
        랭킹
      </span>
    </router-link>
    
    <router-link 
    v-if="isLoggedIn" 
    to="/mypage" 
    class="group intro-btn relative flex items-center justify-center cursor-pointer hover:opacity-80 transition-all duration-100 ease-in-out w-[9rem] h-[2.7rem] text-[#FFC954] mt-[4px] active:translate-x-[4px] active:translate-y-[4px] text-[24px] font-['PfStardust30S'] font-normal leading-none"
    style="--btn-highlight: #FFD966;"
    >
    <IntroButtonBg class="absolute inset-0 w-full h-full drop-shadow-[4px_4px_0_#fff2cc] transition-all duration-100 group-active:drop-shadow-none" />
    <span class="relative z-10 pt-0.9 text-[var(--color-choco)]">
      마이페이지
    </span>
  </router-link>

  <router-link 
    v-if="isLoggedIn"
    to="/lobby" 
    class="group intro-btn relative flex items-center justify-center cursor-pointer hover:opacity-80 transition-all duration-100 ease-in-out w-[8rem] h-[2.7rem] text-[#FFC954] mt-[4px] active:translate-x-[4px] active:translate-y-[4px] text-[24px] font-['PfStardust30S'] font-normal leading-none"
    style="--btn-highlight: #FFD966;"
  >
    <IntroButtonBg class="absolute inset-0 w-full h-full drop-shadow-[4px_4px_0_#fff2cc] transition-all duration-100 group-active:drop-shadow-none" />
    <span class="relative z-10 pt-0.9 text-[var(--color-choco)]">
      방목록
    </span>
  </router-link>

      <button 
        v-if="!isLoggedIn" 
        @click="kakaoLogin" 
        class="group intro-btn relative flex items-center justify-center cursor-pointer hover:opacity-80 transition-all duration-100 ease-in-out w-[8rem] h-[2.7rem] text-[#FFC954] bg-transparent border-none mt-[4px] active:translate-x-[4px] active:translate-y-[4px] text-[24px] font-['PfStardust30S'] font-normal leading-none"
        style="--btn-highlight: #FFD966;"
      >
        <IntroButtonBg class="absolute inset-0 w-full h-full drop-shadow-[4px_4px_0_#fff2cc] transition-all duration-100 group-active:drop-shadow-none" />
        <span class="relative z-10 pt-0.9 text-[var(--color-choco)]">
          로그인
        </span>
      </button>

      <button 
        v-else 
        @click="logout" 
        class="group intro-btn relative flex items-center justify-center cursor-pointer hover:opacity-80 transition-all duration-100 ease-in-out w-[8rem] h-[2.7rem] text-[#FFD966] bg-transparent border-none mt-[4px] active:translate-x-[4px] active:translate-y-[4px] text-[24px] font-['PfStardust30S'] font-normal leading-none"
        style="--btn-highlight: #FFEAAC;"
      >
        <IntroButtonBg class="absolute inset-0 w-full h-full drop-shadow-[4px_4px_0_#fff2cc] transition-all duration-100 group-active:drop-shadow-none" />
        <span class="relative z-10 pt-0.9 text-[var(--color-choco)]">
          로그아웃
        </span>
      </button>
    </div>

    <div 
      v-if="isMenuOpen" 
      class="absolute top-[75px] left-0 w-full
            bg-transparent backdrop-blur-md
            flex flex-col items-center gap-4 py-8
            xl:hidden shadow-lg"
    >


      <router-link 
        to="/introduction" 
        @click="isMenuOpen = false"
        class="group relative flex items-center justify-center cursor-pointer w-[9rem] h-[2.7rem] text-[#FFD966] active:translate-x-[2px] active:translate-y-[2px] text-[24px] font-['PfStardust30S'] font-normal leading-none"
        style="--btn-highlight: #FFEAAC;"
      >
        <IntroButtonBg class="absolute inset-0 w-full h-full drop-shadow-[4px_4px_0_#fff2cc] transition-all duration-100 group-active:drop-shadow-none" />
        <span class="relative z-10 pt-0.9 text-[var(--color-choco)]">버터디 소개</span>
      </router-link>

      <template v-if="isLoggedIn">
        <router-link 
          to="/lobby" 
          @click="isMenuOpen = false"
          class="group relative flex items-center justify-center cursor-pointer w-[9rem] h-[2.7rem] text-[#FFC954] active:translate-x-[2px] active:translate-y-[2px] text-[24px] font-['PfStardust30S'] font-normal leading-none"
          style="--btn-highlight: #FFD966;"
        >
          <IntroButtonBg class="absolute inset-0 w-full h-full drop-shadow-[4px_4px_0_#fff2cc] transition-all duration-100 group-active:drop-shadow-none" />
          <span class="relative z-10 pt-0.9 text-[var(--color-choco)]">방목록</span>
        </router-link>

        <router-link 
          to="/ranking" 
          @click="isMenuOpen = false"
          class="group relative flex items-center justify-center cursor-pointer w-[9rem] h-[2.7rem] text-[#FFC954] active:translate-x-[2px] active:translate-y-[2px] text-[24px] font-['PfStardust30S'] font-normal leading-none"
          style="--btn-highlight: #FFD966;"
        >
          <IntroButtonBg class="absolute inset-0 w-full h-full drop-shadow-[4px_4px_0_#fff2cc] transition-all duration-100 group-active:drop-shadow-none" />
          <span class="relative z-10 pt-0.9 text-[var(--color-choco)]">랭킹</span>
        </router-link>

        <router-link 
          to="/mypage" 
          @click="isMenuOpen = false"
          class="group relative flex items-center justify-center cursor-pointer w-[9rem] h-[2.7rem] text-[#FFC954] active:translate-x-[2px] active:translate-y-[2px] text-[24px] font-['PfStardust30S'] font-normal leading-none"
          style="--btn-highlight: #FFD966;"
        >
          <IntroButtonBg class="absolute inset-0 w-full h-full drop-shadow-[4px_4px_0_#fff2cc] transition-all duration-100 group-active:drop-shadow-none" />
          <span class="relative z-10 pt-0.9 text-[var(--color-choco)]">마이페이지</span>
        </router-link>

        <button 
          @click="handleLogout" 
          class="group relative flex items-center justify-center cursor-pointer w-[9rem] h-[2.7rem] text-[#FFD966] bg-transparent border-none active:translate-x-[2px] active:translate-y-[2px] text-[24px] font-['PfStardust30S'] font-normal leading-none"
          style="--btn-highlight: #FFEAAC;"
        >
          <IntroButtonBg class="absolute inset-0 w-full h-full drop-shadow-[4px_4px_0_#fff2cc] transition-all duration-100 group-active:drop-shadow-none" />
          <span class="relative z-10 pt-0.9 text-[var(--color-choco)]">로그아웃</span>
        </button>
      </template>

      <button 
        v-else 
        @click="kakaoLogin" 
        class="group relative flex items-center justify-center cursor-pointer w-[9rem] h-[2.7rem] text-[#FFC954] bg-transparent border-none active:translate-x-[2px] active:translate-y-[2px] text-[24px] font-['PfStardust30S'] font-normal leading-none"
        style="--btn-highlight: #FFD966;"
      >
        <IntroButtonBg class="absolute inset-0 w-full h-full drop-shadow-[4px_4px_0_#fff2cc] transition-all duration-100 group-active:drop-shadow-none" />
        <span class="relative z-10 pt-0.9 text-[var(--color-choco)]">로그인</span>
      </button>

    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'; // ref, watch 추가
import { useRouter, useRoute } from 'vue-router'; // useRoute 추가
import { useAuthStore } from '@/stores/authStore';
import LogoIcon from '@/assets/logo.svg?component';
import IntroButtonBg from '@/assets/icons/IntroButtonBg.vue';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute(); // 라우트 변경 감지용

const isLoggedIn = computed(() => authStore.isLoggedIn);
const logoLink = computed(() => (isLoggedIn.value ? '/user' : '/'));

// --- 모바일 메뉴 관련 로직 ---
const isMenuOpen = ref(false);

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value;
};

// 라우트가 변경되면(페이지 이동 시) 메뉴 닫기
watch(route, () => {
  isMenuOpen.value = false;
});

const kakaoLogin = () => {
  const clientId = import.meta.env.VITE_KAKAO_CLIENT_ID;
  const redirectUri = import.meta.env.VITE_KAKAO_REDIRECT_URI;
  const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}`;
  window.location.href = kakaoAuthUrl;
};

const handleLogout = () => {
  isMenuOpen.value = false; // 모바일 메뉴 닫기
  logout();
};

const logout = () => {
  authStore.clearAuth();
  router.push('/guest');
};
</script>



