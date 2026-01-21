// 
import axios from '@/shared/api/axios.config';

export const authAPI = {
  // 카카오 OAuth 콜백 처리
  kakaoCallback: (code: string) => {
    return axios.post('/auth/kakao/callback', { code });
  },

  // 필요한 다른 API 메서드들
  login: (email: string, password: string) => {
    return axios.post('/auth/login', { email, password });
  },

  logout: () => {
    return axios.post('/auth/logout');
  },

  signup: (userData: Record<string, unknown>) => {
    return axios.post('/auth/signup', userData);
  }
};