import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path' // 경로 별칭 설정용 
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    tailwindcss()
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'), // @를 src 폴더로 인식
    },
  },
  server: {
    port: 3030, // 백엔드 가이드에 따라 포트를 3030으로 고정
    proxy: {
      // 브라우저에서 /api로 시작하는 요청을 보내면 백엔드 서버(80)로 전달합니다.
      '/api': {
        // target: 'http://i14a703.p.ssafy.io:80',
        target: 'http://i14a703.p.ssafy.io:8080', // 로컬 백엔드 테스트용
        changeOrigin: true,
        secure: false,
        // 백엔드 API 경로가 /api로 시작하므로 rewrite는 하지 않습니다.
      },
      // [추가] WebSocket 요청도 백엔드로 프록시 (CORS 해결)
      '/ws': {
        // target: 'http://i14a703.p.ssafy.io:80',
        target: 'http://i14a703.p.ssafy.io:8081', // Interceptor가 8081을 요구하므로 8081로 연결
        changeOrigin: true,
        ws: true,
        secure: false
      }
    }
  }
})
