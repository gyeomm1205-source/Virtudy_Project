<template>
  <div>
    <h1>환영합니다!</h1>
    <p>로그인 및 온보딩이 성공적으로 완료되었습니다.</p>
    <p v-if="authStore.isLoggedIn">당신은 로그인 된 상태입니다.</p>
    <button @click="handleLogout">로그아웃</button>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  authStore.clearAuth();
  alert('로그아웃 되었습니다.');
  router.push('/login');
};
</script>

<style scoped>
div {
  text-align: center;
  padding-top: 50px;
}
</style>
