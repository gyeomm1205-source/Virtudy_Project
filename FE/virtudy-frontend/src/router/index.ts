import { createRouter, createWebHistory } from 'vue-router'
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