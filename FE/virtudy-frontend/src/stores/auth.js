import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  // 상태 (State)
  const accessToken = ref(localStorage.getItem('accessToken') || null);
  const userInfo = ref(null); // 추가 정보(닉네임 등) 저장용

  // 게터 (Getters)
  const isLoggedIn = computed(() => !!accessToken.value);

  // 액션 (Actions)
  // 1. 로그인 성공 시 토큰 저장
  const setToken = (token) => {
    accessToken.value = token;
    localStorage.setItem('accessToken', token); // 브라우저 새로고침 대비
  };

  // 2. 로그아웃 시 초기화
  const clearAuth = () => {
    accessToken.value = null;
    userInfo.value = null;
    localStorage.removeItem('accessToken');
  };

  return {
    accessToken,
    userInfo,
    isLoggedIn,
    setToken,
    clearAuth
  };
});