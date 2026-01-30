import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path' // 경로 별칭 설정용 
import tailwindcss from '@tailwindcss/vite'
import svgLoader from 'vite-svg-loader' // SVG 로더 플러그인 추가 (아바타를 위함)

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    tailwindcss(),
    svgLoader({
      defaultImport: 'component' // SVG를 기본적으로 Vue 컴포넌트로 import
    }),
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
        target: 'http://i14a703.p.ssafy.io:80',
        // target: 'http://i14a703.p.ssafy.io:8080', // 로컬 백엔드 테스트용
        changeOrigin: true,
        secure: false,
      },
      // [추가] WebSocket 요청도 백엔드로 프록시 (CORS 해결)
      '/ws': {
        target: 'http://i14a703.p.ssafy.io:80',
        // target: 'http://i14a703.p.ssafy.io:8081', // Interceptor가 8081을 요구하므로 8081로 연결
        changeOrigin: true,
        ws: true,
        secure: false
      },
      // '/fastapi'로 시작하는 요청이 오면 
      // -> https://i14a703.p.ssafy.io 로 보낸다
      '/fastapi': {
        target: 'https://i14a703.p.ssafy.io',
        // changeOrigin: true,
        // secure: false, // HTTPS 인증서 에러 무시 (필요시)
        rewrite: (path) => path.replace(/^\/fastapi/, '') // 경로 수정이 필요 없다면 주석 처리
      },
    }
  }
})
