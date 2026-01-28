import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

// 페이지 컴포넌트 임포트
import GuestPage from '@/features/main/pages/GuestPage.vue';
import UserPage from '@/features/main/pages/UserPage.vue';
import OAuthCallbackPage from '@/features/auth/pages/OAuthCallbackPage.vue'; // 콜백 처리를 위한 페이지
import TermsOfServicePage from '@/features/onboarding/pages/TermsOfServicePage.vue';
import OnboardingSurveyPage from '@/features/onboarding/pages/OnboardingSurveyView.vue';
import StudyRoom from '@/features/study-room/ui/StudyRoom.vue';
import IntroductionPage from '@/features/introduction/pages/IntroductionPage.vue';
import RankingPage from '@/features/ranking/pages/RankingPage.vue';
import MyPage from '@/features/mypage/pages/MyPage.vue';
import LobbyPage from '@/features/lobby/pages/LobbyPage.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/study',
      name: 'study',
      component: StudyRoom
    },
    {
      path: '/',
      name: 'home',
      // 로그인 상태에 따라 GuestPage 또는 UserPage로 리다이렉트
      redirect: () => {
        const authStore = useAuthStore();
        // 로그인 상태면 UserPage('/user')로, 아니면 GuestPage('/guest')로 보냄
        if (authStore.isLoggedIn) {
          return { name: 'user' }; 
        } else {
          return { name: 'guest' }; 
        }
      }
    },
    {
      path: '/guest',
      name: 'guest',
      component: GuestPage,
      beforeEnter: (to, from, next) => {
        const authStore = useAuthStore();
        // 이미 로그인한 유저가 /guest로 오면 /user로 보냄
        if (authStore.isLoggedIn) {
          return next({ name: 'user' });
        }
        return next();
      }
    },
    {
      path: '/user',
      name: 'user',
      component: UserPage,
      beforeEnter: (to, from, next) => {
        const authStore = useAuthStore();
        // 로그인 안 한 유저가 /user로 오면 /guest로 보냄
        if (!authStore.isLoggedIn) {
          return next({ name: 'guest' });
        }
        return next();
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
      beforeEnter: (to, from, next) => {
        const authStore = useAuthStore();

        // 1. Pinia store에 정보가 있으면 통과
        if (authStore.signupInfo) {
          return next();
        }

        // 2. Pinia에 없으면 localStorage 확인 (새로고침 대비)
        const storedInfo = localStorage.getItem('signupInfo');
        if (storedInfo && storedInfo !== 'undefined') {
          try {
            // localStorage 정보를 다시 Pinia에 저장하고 통과
            authStore.setSignupInfo(JSON.parse(storedInfo));
            return next();
          } catch (e) {
            console.error(
              'signupInfo 파싱 오류. 게스트 페이지로 리디렉션합니다.',
              e
            );
            return next({ name: 'guest' }); // 파싱 실패 시 접근 거부
          }
        }

        // 3. 둘 다 없으면 비정상 접근으로 간주, 게스트 페이지로 리디렉션
        alert('비정상적인 접근입니다. 로그인부터 다시 진행해주세요.');
        return next({ name: 'guest' });
      }
    },
    {
      path: '/onboarding/survey',
      name: 'survey',
      component: OnboardingSurveyPage,
      beforeEnter: (to, from, next) => {
        const authStore = useAuthStore();
        // 1. Pinia나 LocalStorage에 가입 정보가 있는지 확인
        const hasInfo = authStore.signupInfo || localStorage.getItem('signupInfo');

        // 2. 정보가 없으면 내쫓기
        if (!hasInfo) {
          alert("잘못된 접근입니다. 처음부터 다시 시도해주세요.");
          return next({ name: 'guest' });
        }
        return next(); // 통과
      }
    },
    {
      path: '/introduction',
      name: 'introduction',
      component: IntroductionPage
    },
    {
      path: '/ranking',
      name: 'ranking',
      component: RankingPage
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: MyPage,
      beforeEnter: () => {
        const authStore = useAuthStore();
        if (!authStore.isLoggedIn) {
          return { name: 'guest' };
        }
      }
    },
    {
      path: '/lobby',
      name: 'lobby',
      component: LobbyPage
    }
  ]
});

export default router;