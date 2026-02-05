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
      svgoConfig: {
        plugins: [
          // 기본 설정 유지하되, ID 제거 플러그인만 비활성화
          {
            name: 'preset-default',
            params: {
              overrides: {
                removeTitle: false,
                removeViewBox: false,
                cleanupIds: false, // ID를 제거하지 않음
              },
            },
          },
          // 혹은 명시적으로 prefixIds 등을 설정할 수도 있음
        ],
      },
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
      '/api': {
        target: 'http://localhost:8080', // 로컬 백엔드 (기본값)
        // target: 'http://i14a703.p.ssafy.io:80', // 진짜 배포 서버
        changeOrigin: true,
        secure: false,
      },
      // [추가] WebSocket 요청도 백엔드로 프록시 (CORS 해결)
      '/ws': {
        target: 'http://localhost:8081', // 로컬 소켓 서버
        // target: 'http://i14a703.p.ssafy.io:80', // 진짜 배포 서버
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
