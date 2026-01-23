// 
import axios from '@/shared/api/axios.config';

export const authAPI = {
  // 카카오 OAuth 콜백 처리
  //수정 전: json bobdy로 보내던 것을 수정
  //수정 후: 쿼리 스트링으로 보내도록 수정(백엔드 @RequestParam 대응ㅇ)
  kakaoCallback: (code: string) => {
    return axios.post('/auth/kakao/callback', null,{params: {code} });
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