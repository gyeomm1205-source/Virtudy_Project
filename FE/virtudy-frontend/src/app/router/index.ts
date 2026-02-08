import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

// 페이지 컴포넌트 임포트
import GuestPage from '@/features/main/pages/GuestPage.vue';
import UserPage from '@/features/main/pages/UserPage.vue';
import OAuthCallbackPage from '@/features/auth/pages/OAuthCallbackPage.vue'; // 콜백 처리를 위한 페이지
import TermsOfServicePage from '@/features/onboarding/pages/TermsOfServicePage.vue';
import OnboardingSurveyPage from '@/features/onboarding/pages/OnboardingSurveyView.vue';
import StudyRoomPage from '@/features/study-room/pages/StudyRoomPage.vue';
import IntroductionPage from '@/features/introduction/pages/IntroductionPage.vue';
import RankingPage from '@/features/ranking/pages/RankingPage.vue';
import MyPage from '@/features/mypage/pages/MyPage.vue';
import LobbyPage from '@/features/lobby/pages/LobbyPage.vue';
import ReportPage from '@/features/report/pages/ReportPage.vue';
import AvatarCreationPage from '@/features/avatar/pages/AvatarCreationPage.vue';
import TutorialPage from '@/features/introduction/pages/TutorialPage.vue';
import { useUiStore } from '@/stores/uiStore';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/study',
      name: 'study',
      component: StudyRoomPage,
      meta: { hideGlobalNav: true }
    },
    {
      path: '/study/:roomId',
      name: 'StudyRoom',
      component: StudyRoomPage,
      meta: { hideGlobalNav: true }
    },
    {
      path: '/',
      name: 'home',
      redirect: () => {
        const authStore = useAuthStore();
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
        if (!authStore.isLoggedIn) {
          return next({ name: 'guest' });
        }
        return next();
      }
    },
    {
      path: '/auth/kakao/callback',
      name: 'kakao-callback',
      component: OAuthCallbackPage
    },
    {
      path: '/onboarding/terms',
      name: 'terms',
      component: TermsOfServicePage,
      beforeEnter: async (to, from, next) => {
        const authStore = useAuthStore();
        const uiStore = useUiStore();

        // 1. Pinia 통과
        if (authStore.signupInfo) return next();

        // 2. localStorage 확인
        const storedInfo = localStorage.getItem('signupInfo');
        if (storedInfo && storedInfo !== 'undefined') {
          try {
            authStore.setSignupInfo(JSON.parse(storedInfo));
            return next();
          } catch (e) {
            console.error('파싱 오류', e);
            return next({ name: 'guest' });
          }
        }

        // 3. 커스텀 모달 호출 (await로 대기)
        await uiStore.openAlert(
          '비정상적인 접근입니다.\n로그인부터 다시 진행해주세요.',
          '접근 제한'
        );

        return next({ name: 'guest' });
      }
    },
    {
      path: '/onboarding/survey',
      name: 'survey',
      component: OnboardingSurveyPage,
      // async 키워드 추가
      beforeEnter: async (to, from, next) => {
        const authStore = useAuthStore();
        const uiStore = useUiStore(); // uiStore 사용

        const hasInfo = authStore.signupInfo || localStorage.getItem('signupInfo');

        if (!hasInfo) {
          // alert 대체 -> await uiStore.openAlert
          await uiStore.openAlert(
            "잘못된 접근입니다.\n처음부터 다시 시도해주세요.",
            "접근 오류"
          );
          return next({ name: 'guest' });
        }
        return next();
      }
    },
    {
      path: '/introduction',
      name: 'introduction',
      component: IntroductionPage
    },
    {
      path: '/tutorial',
      name: 'tutorial',
      component: TutorialPage,
      meta: {
        hideGlobalNav: true,
      }
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
      beforeEnter: (to, from, next) => {
        const authStore = useAuthStore();
        if (!authStore.isLoggedIn) {
          return next({ name: 'guest' });
        }
        return next();
      }
    },
    {
      path: '/avatar/create',
      name: 'avatar-create',
      component: AvatarCreationPage,
      // async 키워드 추가
      beforeEnter: async (to, from, next) => {
        const authStore = useAuthStore();
        const uiStore = useUiStore(); // uiStore 사용

        if (!authStore.isLoggedIn) {
          // alert 대체 -> await uiStore.openAlert
          await uiStore.openAlert(
            '로그인이 필요한 서비스입니다.', 
            '알림'
          );
          return next({ name: 'guest' });
        }
        return next();
      }
    },
    {
      path: '/lobby',
      name: 'lobby',
      component: LobbyPage
    },
    {
      path: '/report',
      name: 'report',
      component: ReportPage
    }
  ]
});

export default router;