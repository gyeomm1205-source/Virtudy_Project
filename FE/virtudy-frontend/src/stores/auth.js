import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  // 상태 (State)
  const accessToken = ref(localStorage.getItem('accessToken') || null);
  const signupInfo = ref(JSON.parse(localStorage.getItem('signupInfo')) || null); // 신규 유저 임시 정보
  const userInfo = ref(null); // 추가 정보(닉네임 등) 저장용

  // 게터 (Getters)
  const isLoggedIn = computed(() => !!accessToken.value);

  // 액션 (Actions)
  // 1. 로그인 성공 시 토큰 저장
  const setToken = (token) => {
    accessToken.value = token;
    localStorage.setItem('accessToken', token); // 브라우저 새로고침 대비
  };

  // 2. 신규 가입 플로우를 위한 임시 정보 저장
  const setSignupInfo = (info) => {
    signupInfo.value = info;
    localStorage.setItem('signupInfo', JSON.stringify(info)); // 페이지 이동 대비
  };
  
  // 3. 임시 정보 클리어
  const clearSignupInfo = () => {
    signupInfo.value = null;
    localStorage.removeItem('signupInfo');
  };

  // 4. 로그아웃 또는 전체 인증 정보 초기화
  const clearAuth = () => {
    accessToken.value = null;
    userInfo.value = null;
    signupInfo.value = null; // 임시 정보도 함께 클리어
    localStorage.removeItem('accessToken');
    localStorage.removeItem('signupInfo');
  };

  return {
    accessToken,
    signupInfo,
    userInfo,
    isLoggedIn,
    setToken,
    setSignupInfo,
    clearSignupInfo,
    clearAuth
  };
});