import axios, { type AxiosError } from 'axios';

// Axios의 기본 타입을 확장하여 커스텀 속성을 추가합니다.
declare module 'axios' {
  export interface AxiosRequestConfig {
    _retry?: boolean; // 재시도 여부를 나타내는 플래그
  }
}

// 1. Axios 인스턴스 생성
const instance = axios.create({
  // 운영 환경과 로컬 환경 주소를 유연하게 대처하기 위해 환경변수 사용 권장
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  withCredentials: true, // RT(HttpOnly Cookie)를 주고받기 위해 필수
  headers: {
    'Content-Type': 'application/json',
  },
});

// 2. 요청(Request) 인터셉터: 모든 요청 헤더에 AT를 자동으로 삽입
instance.interceptors.request.use(
  (config) => {
    // 로컬 스토리지에서 직접 꺼냅니다. (정책: AT는 변수/스토리지 관리)
    const token = localStorage.getItem('accessToken');
    if (token) {
      // 백엔드 가이드: AT는 Auth 헤더에 명시
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// 3. 응답(Response) 인터셉터: 백엔드 에러 규격 처리
instance.interceptors.response.use(
  (response) => response, // 성공 시 그대로 반환
  async (error: AxiosError<any>) => {
    const { config, response } = error;

    // config._retry 플래그를 확인하여 재발급 요청은 딱 한 번만 시도하도록 제한 (무한 루프 방지)
    // 백엔드 가이드: 401(Unauthorized) 또는 특정 코드(AUTH_004) 체크
    if (config && response && response.status === 401 && response.data?.code === 'AUTH_004' && !config._retry) {
      config._retry = true; // 현재 요청이 재시도 중임을 표시

      // 토큰 만료 에러(AUTH_004)일 경우 재발급 시도
      try {
        // RT는 쿠키에 있으므로 별도 데이터 없이 재발급 요청만 보냄
        // instance 대신 axios(기본)를 사용하여 재발급 요청 자체가 인터셉터에 걸리지 않게 함 (순환 방지)
        const { data } = await axios.post(`${instance.defaults.baseURL}/auth/reissue`, {}, { withCredentials: true });

        // 새 AT 저장 (백엔드가 Json Body로 주기로 함)
        const newAccessToken = data.accessToken;
        // 동적 import로 컨텍스트 문제 해결: 인터셉터 실행 시점에 스토어를 가져옴
        const { useAuthStore } = await import('@/stores/authStore');
        const authStore = useAuthStore();
        authStore.setToken(newAccessToken);

        // 실패했던 이전 요청의 헤더를 새 토큰으로 교체하고 재시도
        config.headers.Authorization = `Bearer ${newAccessToken}`;
        return instance(config);
      } catch (refreshError) {
        // 재발급 실패 시 인증 정보 클리어 및 로그인 페이지로 리디렉션
        const { useAuthStore } = await import('@/stores/authStore');
        const authStore = useAuthStore();
        authStore.clearAuth(); // 스토어와 로컬스토리지를 함께 비우기
        window.location.href = '/login'; // 로그인 페이지로 이동
        return Promise.reject(refreshError);
      }
    }

    // 그 외 에러는 백엔드가 정의한 에러 메시지를 활용해 알림 처리
    // AUTH_004(재발급 흐름)일 때는 alert을 띄우지 않도록 조건 추가
    if (response?.data?.code !== 'AUTH_004') {
      const errorMessage = response?.data?.message || '문제가 발생했습니다.';
      alert(errorMessage);
    }

    return Promise.reject(error);
  }
);

export default instance;