import { createApp } from 'vue'
import '@/assets/styles/global.css' // 전역 스타일시트 임포트
import router from './router/index' // 라우터 임포트
import { pinia } from './store' // Pinia 임포트

import App from "./App.vue";

const app = createApp(App)

// Pinia 등록
app.use(pinia)

// 라우터 등록
app.use(router)

app.mount('#app')