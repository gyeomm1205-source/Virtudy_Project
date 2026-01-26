<template>
  <nav class="nav-container">
    <div class="nav-left">
      <router-link :to="logoLink" class="logo">Virtudy</router-link>
      <router-link to="/introduction" class="nav-link">소개하기</router-link>
    </div>
    <div class="nav-right">
      <router-link to="/ranking" class="nav-link">랭킹</router-link>
      <router-link v-if="isLoggedIn" to="/mypage" class="nav-link">마이페이지</router-link>
      <button v-if="!isLoggedIn" @click="kakaoLogin" class="login-button">
        카카오로 시작하기
      </button>
      <button v-else @click="logout" class="login-button">로그아웃</button>
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

<style scoped>
.nav-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: #fff;
  border-bottom: 1px solid #eee;
}

.nav-left,
.nav-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  text-decoration: none;
}

.nav-link {
  font-size: 1rem;
  color: #555;
  text-decoration: none;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #007bff;
}

.login-button {
  padding: 0.5rem 1rem;
  font-size: 1rem;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
  background-color: #ffeb00;
  color: #3c1e1e;
}
</style>
