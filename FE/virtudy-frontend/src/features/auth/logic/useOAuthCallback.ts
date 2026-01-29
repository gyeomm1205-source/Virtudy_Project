
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { authAPI } from '../api/authAPI';

/**
 * OAuth 카카오 콜백 처리 로직
 * - 인가 코드 추출
 * - 백엔드에 코드 전송
 * - 신규/기존 유저 판단
 * - 리다이렉트
 */
export const useOAuthCallback = () => {
  const route = useRoute();
  const router = useRouter();
  const authStore = useAuthStore();

  const loading = ref(false);
  const error = ref<string | null>(null);

  /**
   * OAuth 콜백 처리 메인 로직
   */
  const handleOAuthCallback = async (): Promise<void> => {
    loading.value = true;
    error.value = null;

    try {
      //  URL에서 인가 코드(code) 추출
      const code = route.query.code as string;

      if (!code) {
        throw new Error('비정상적인 접근입니다. 인가 코드가 없습니다.');
      }

      // 백엔드에 인가 코드 전송
      const response = await authAPI.kakaoCallback(code);

      // 디버깅 코드 (나중에 지워야함)-------------------
      console.log(response)
      // -----------------------------

      // 응답에서 토큰 및 신규/기존 유저 여부 추출
      const { needSignup: isNewUser, accessToken, ...signupInfo } = response.data;
      
      if (isNewUser) {
        // 신규 유저 → 임시 가입 정보 저장 후 약관 페이지로
        authStore.setSignupInfo(signupInfo);
        alert('Virtudy에 오신 것을 환영합니다! 몇 가지 추가 정보만 입력해주세요.');
        await router.push({ name: 'terms' });
      } else {
        // 기존 유저
        const { nickName, userId } = response.data; // userId 추출
        if (!accessToken) {
          // isNewUser가 false인데 토큰이 없는 경우 -> 서버 오류
          throw new Error(
            '로그인에 실패했습니다. 서버에서 인증 토큰을 받지 못했습니다.',
          );
        }
        // 토큰 저장 후 유저 페이지로
        authStore.setToken(accessToken);
        if (nickName) {
          authStore.setUserInfo({
            userId: userId || 'unknown', // userId 필수
            nickName,
            email: '',
            avatarImageUrl: '',
            jobType: '',
            tier: ''
          });
        }
        await router.push({ name: 'user' });
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : '로그인 처리 실패';
      error.value = message;
      console.error('OAuth 콜백 처리 실패:', err);

      alert(`${message}\n문제가 지속되면 관리자에게 문의하세요.`);
      await router.push({ name: 'guest' });
    } finally {
      loading.value = false;
    }
  };

  return {
    loading,
    error,
    handleOAuthCallback,
  };
};

