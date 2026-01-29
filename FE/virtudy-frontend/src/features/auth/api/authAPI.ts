// 
import axios from '@/shared/api/axios.config';

export const authAPI = {
  // 카카오 OAuth 콜백 처리
  kakaoCallback: (code: string) => {
    return axios.post('/auth/kakao/callback', null, { params: { code } });
  },

  // 회원가입 (추가 정보 입력)
  signup: (userData: Record<string, unknown>) => {
    return axios.post('/auth/signup', userData);
  },

  // 로그아웃
  logout: () => {
    return axios.post('/auth/logout');
  },
  // 회원탈퇴
  withdraw: () => {
    return axios.delete('/auth/withdraw');
  }
};