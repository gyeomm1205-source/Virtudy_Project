import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api from '@/shared/api/axios.config'; // 설정된 axios 인스턴스

interface User {
  nickName: string;
  // Add other user-related fields here if needed
}

export const useAuthStore = defineStore('auth', () => {
  // 상태 (State)
  const accessToken = ref(localStorage.getItem('accessToken') || null);
  const signupInfo = ref(localStorage.getItem('signupInfo') ? JSON.parse(localStorage.getItem('signupInfo')!) : null); // 신규 유저 임시 정보
  const userInfo = ref<User | null>(localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')!) : null); // 추가 정보(닉네임 등) 저장용

  // 게터 (Getters)
  const isLoggedIn = computed(() => !!accessToken.value);

  // 액션 (Actions)
  // 1. 로그인 성공 시 토큰 저장
  const setToken = (token: string) => {
    accessToken.value = token;
    localStorage.setItem('accessToken', token); // 브라우저 새로고침 대비
  };

  // 2. 신규 가입 플로우를 위한 임시 정보 저장
  interface SignupInfo {
    [key: string]: unknown;
  }

  const setSignupInfo = (info: SignupInfo): void => {
    signupInfo.value = info;
    localStorage.setItem('signupInfo', JSON.stringify(info)); // 페이지 이동 대비
  };
  
  // 3. 임시 정보 클리어
  const clearSignupInfo = () => {
    signupInfo.value = null;
    localStorage.removeItem('signupInfo');
  };

  // 4. 유저 정보 저장 (닉네임 등)
  const setUserInfo = (user: User) => {
    userInfo.value = user;
    localStorage.setItem('userInfo', JSON.stringify(user));
  };

  // 5. 로그아웃 또는 전체 인증 정보 초기화
  const clearAuth = () => {
    accessToken.value = null;
    userInfo.value = null;
    signupInfo.value = null; // 임시 정보도 함께 클리어
    localStorage.removeItem('accessToken');
    localStorage.removeItem('signupInfo');
    localStorage.removeItem('userInfo');
  };

  // [추가] 백엔드에 내 정보 요청하는 함수
  const fetchUserInfo = async () => {
    if (!accessToken.value) return; // 토큰 없으면 요청 X

    try {
      // 작성하신 MemberController의 주소로 요청
      const { data } = await api.get('/members/profile');
      
      // 받아온 데이터를 스토어에 저장
      userInfo.value = data; 
      console.log('내 정보 가져오기 성공:', data);
    } catch (error) {
      console.error('내 정보 불러오기 실패:', error);
    }
  };

  return {
    accessToken,
    signupInfo,
    userInfo,
    isLoggedIn,
    setToken,
    setSignupInfo,
    clearSignupInfo,
    setUserInfo,
    clearAuth,
    fetchUserInfo,
  };
});