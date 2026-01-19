import { createApp } from 'vue'
import { createPinia } from 'pinia'
import '@/app/styles/global.css' // 전역 스타일시트 임포트


import App from './App.vue'

const app = createApp(App)

// Pinia 등록
app.use(createPinia())

// Router는 나중에 src/app/providers/router 설정 후 등록
// import { router } from '@/app/providers/router'
// app.use(router)

app.mount('#app')