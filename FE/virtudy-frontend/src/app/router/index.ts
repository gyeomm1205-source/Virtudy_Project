import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

// 페이지 컴포넌트 임포트
import GuestPage from '@/features/auth/pages/GuestPage.vue';
import UserPage from '@/features/auth/pages/UserPage.vue';
import OAuthCallbackPage from '@/features/auth/pages/OAuthCallbackPage.vue'; // 콜백 처리를 위한 페이지
import TermsOfServicePage from '@/features/onboarding/pages/TermsOfServicePage.vue';
import OnboardingSurveyPage from '@/features/onboarding/pages/OnboardingSurveyView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      // 로그인 상태에 따라 GuestPage 또는 UserPage로 리다이렉트
      redirect: () => {
        const authStore = useAuthStore();
        return authStore.isLoggedIn ? { name: 'user' } : { name: 'guest' };
      }
    },
    {
      path: '/guest',
      name: 'guest',
      component: GuestPage,
      beforeEnter: () => {
        const authStore = useAuthStore();
        if (authStore.isLoggedIn) {
          // 이미 로그인했다면 UserPage로
          return { name: 'user' };
        }
      }
    },
    {
      path: '/user',
      name: 'user',
      component: UserPage,
      beforeEnter: () => {
        const authStore = useAuthStore();
        if (!authStore.isLoggedIn) {
          // 로그인을 안했다면 GuestPage로
          return { name: 'guest' };
        }
      }
    },
    {
      path: '/login/callback/kakao',
      name: 'kakao-callback',
      component: OAuthCallbackPage
    },
    {
      path: '/onboarding/terms',
      name: 'terms',
      component: TermsOfServicePage,
      beforeEnter: () => {
        const authStore = useAuthStore();
        // 임시 정보가 없으면 비정상 접근으로 간주하여 guest 페이지로
        if (!authStore.signupInfo) {
          return { name: 'guest' };
        }
      }
    },
    {
      path: '/onboarding/survey',
      name: 'survey',
      component: OnboardingSurveyPage,
      beforeEnter: (to, from) => {
        const authStore = useAuthStore();
        // 약관 동의 페이지에서 온 것이 아니거나, 임시 정보가 없으면 비정상 접근
        if (from.name !== 'terms' || !authStore.signupInfo) {
          return { name: 'guest' };
        }
      }
    }
  ]
});

export default router;