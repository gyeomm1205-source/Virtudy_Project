<template>
  <div>로그인 처리 중입니다. 잠시만 기다려 주세요...</div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import instance from '@/api/index'; //axios 인스턴스
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

onMounted(async () => {
  // 1. URL 쿼리 스트링에서 'code' 추출
  const code = route.query.code;

  if (code) {
    try {
      // 2. 백엔드 명세: POST /api/v1/auth/login/kakao
      // 바디에 인가 코드를 담아 보냅니다.
      const response = await instance.post('/v1/auth/login/kakao', { code });
      
      // 3. 백엔드 정책: 응답 바디의 accessToken 저장
      const { accessToken } = response.data;
      // 수정: localStorage 직접 저장 대신 스토어 액션 사용
      authStore.setToken(accessToken);
      alert('로그인 성공!');
      
      // 4. 메인 화면으로 이동 (또는 추가 정보 입력이 필요하면 온보딩 페이지로)
      router.push('/'); 
    } catch (error) {
      console.error('로그인 에러:', error);
      alert('로그인에 실패했습니다.');
      router.push('/login');
    }
  }
});
</script>