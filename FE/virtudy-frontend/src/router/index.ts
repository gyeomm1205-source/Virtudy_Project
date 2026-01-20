import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import OAuthCallbackView from '@/views/auth/OAuthCallbackView.vue'
import TermsOfServiceView from '@/views/onboarding/TermsOfServiceView.vue'
import OnboardingSurveyView from '@/views/onboarding/OnboardingSurveyView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      // 카카오 Redirect URI 설정과 맞춘 경로
      path: '/login/callback/kakao',
      name: 'kakao-callback',
      component: OAuthCallbackView 
    },
    {
      path: '/terms',
      name: 'terms',
      component: TermsOfServiceView,
    },
    {
      path: '/survey',
      name: 'survey',
      component: OnboardingSurveyView,
    }
  ]
})

export default router