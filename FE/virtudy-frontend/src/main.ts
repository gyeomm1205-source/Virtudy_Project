import { createApp } from 'vue'
import { createPinia } from 'pinia'
import '@/app/styles/global.css' // 전역 스타일시트 임포트
import router from './router' // 라우터 임포트

import App from './App.vue'

const app = createApp(App)

// Pinia 등록
app.use(createPinia())

// 라우터 등록
app.use(router)

app.mount('#app')