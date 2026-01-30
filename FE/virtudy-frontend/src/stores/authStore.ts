import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { jwtDecode } from 'jwt-decode'; // JWT 토큰 (userId) 확인용
import api from '@/shared/api/axios.config'; // 설정된 axios 인스턴스
import type { User, AvatarConfig } from '@/shared/types/common.types';


export const useAuthStore = defineStore('auth', () => {
  // 상태 (State)
  const accessToken = ref(localStorage.getItem('accessToken') || null);
  const userId = ref<string | null>(localStorage.getItem('userId') || null); // userId 토큰에서 추출해 저장
  const signupInfo = ref(localStorage.getItem('signupInfo') ? JSON.parse(localStorage.getItem('signupInfo')!) : null); // 신규 유저 임시 정보
  const userInfo = ref<User | null>(localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')!) : null); // 추가 정보(닉네임 등) 저장용

  // 게터 (Getters)
  const isLoggedIn = computed(() => !!accessToken.value);

  // 액션 (Actions)
  // 1. 로그인 성공 시 토큰 저장
  const setToken = (token: string) => {
    accessToken.value = token;
    localStorage.setItem('accessToken', token); // 브라우저 새로고침 대비

    try {
      const decoded: any = jwtDecode(token);

      // 토큰에서 userId 추출
      const extractedId = decoded.userId;

      if (extractedId) {
        userId.value = extractedId;
        localStorage.setItem('userId', extractedId); // 새로고침 유지
      }
    } catch (error) {
      console.error('🚫 토큰 디코딩 실패:', error);
    }
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
    userId.value = null; // userId 초기화
    userInfo.value = null;
    signupInfo.value = null; // 임시 정보도 함께 클리어
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userId'); // userId 함께 삭제
    localStorage.removeItem('signupInfo');
    localStorage.removeItem('userInfo');
  };

  // 아바타 설정 부분 업데이트 
  const setAvatarConfig = (newAvatar: AvatarConfig) => {
    // 현재 로그인된 유저 정보가 없으면 중단
    if (!userInfo.value) {
      console.warn('⚠️ 로그인 정보가 없어 아바타를 저장할 수 없습니다.');
      return;
    }

    // 스토어 상태(State) 업데이트
    // 기존 userInfo 객체를 복사하고 avatar 부분만 교체
    userInfo.value = {
      ...userInfo.value,
      avatar: newAvatar
    };

    // 로컬 스토리지 동기화 (새로고침 대비)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value));
    
    console.log('✅ 아바타 정보가 스토어에 업데이트되었습니다.');
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
      // 토큰이 유효하지 않은 경우 인증 정보 클리어
      clearAuth();
    }
  };

  // [추가] 앱 초기화 시 토큰 유효성 검증
  const validateToken = async () => {
    if (!accessToken.value) return;

    try {
      await api.get('/members/profile');
    } catch (error) {
      // 토큰이 유효하지 않으면 로그아웃 처리
      console.warn('저장된 토큰이 유효하지 않습니다. 로그아웃 처리합니다.');
      clearAuth();
    }
  };

  return {
    accessToken,
    userId,
    signupInfo,
    userInfo,
    isLoggedIn,
    setToken,
    setSignupInfo,
    clearSignupInfo,
    setUserInfo,
    clearAuth,
    fetchUserInfo,
    validateToken,
    setAvatarConfig,
  };
});