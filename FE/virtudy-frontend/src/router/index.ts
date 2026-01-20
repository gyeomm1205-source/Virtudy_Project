import { createRouter, createWebHistory } from 'vue-router'

// 아래 경로들이 실제 파일 위치와 일치하는지 꼭 확인하세요!
import LoginView from '@/views/auth/LoginView.vue'
import OAuthCallbackView from '@/views/auth/OAuthCallbackView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
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
    }
  ]
})

export default router