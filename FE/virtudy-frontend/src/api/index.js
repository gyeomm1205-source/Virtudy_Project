import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

// 1. Axios 인스턴스 생성
const instance = axios.create({
  // 운영 환경과 로컬 환경 주소를 유연하게 대처하기 위해 환경변수 사용 권장
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  withCredentials: true, // RT(HttpOnly Cookie)를 주고받기 위해 필수
  headers: {
    'Content-Type': 'application/json',
  },
});

// 2. 요청(Request) 인터셉터: 모든 요청 헤더에 AT를 자동으로 삽입
instance.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    const token = authStore.getItem('accessToken'); //Pinia 스토어에서 가져오기
    if (token) {
      // 백엔드 가이드: AT는 Auth 헤더에 명시
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 3. 응답(Response) 인터셉터: 백엔드 에러 규격 처리
instance.interceptors.response.use(
  (response) => response, // 성공 시 그대로 반환
  async (error) => {
    const { config, response } = error;

    // 백엔드 가이드: 401(Unauthorized) 또는 특정 코드(AUTH_004) 체크
    // 추가: config._retry 플래그를 확인하여 재발급 요청은 딱 한 번만 시도하도록 제한 (무한 루프 방지)
    if (response && response.status === 401 && response.data.code === 'AUTH_004' && !config._retry) {
      config._retry = true; // 새 주석: 현재 요청이 재시도 중임을 표시

      // 토큰 만료 에러(AUTH_004)일 경우 재발급 시도
      try {
        // RT는 쿠키에 있으므로 별도 데이터 없이 재발급 요청만 보냄
        // 새 주석: instance 대신 axios(기본)를 사용하여 재발급 요청 자체가 인터셉터에 걸리지 않게 함
        const { data } = await axios.post(`${instance.defaults.baseURL}/v1/auth/reissue`, {}, { withCredentials: true });
        
        // 새 AT 저장 (백엔드가 Json Body로 주기로 함)
        const newAccessToken = data.accessToken;
        localStorage.setItem('accessToken', newAccessToken);

        // 실패했던 이전 요청의 헤더를 새 토큰으로 교체하고 재시도
        config.headers.Authorization = `Bearer ${newAccessToken}`;
        return instance(config);
      } catch (refreshError) {
        // 재발급도 실패하면(RT 만료 등) 로그아웃 처리
        localStorage.removeItem('accessToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    // 그 외 에러는 백엔드가 정의한 에러 메시지를 활용해 알림 처리
    // 새 주석: AUTH_004(재발급 흐름)일 때는 alert을 띄우지 않도록 조건 추가
    if (response?.data?.code !== 'AUTH_004') {
      const errorMessage = response?.data?.message || '문제가 발생했습니다.';
      alert(errorMessage);
    }
    
    return Promise.reject(error);
  }
);

export default instance;