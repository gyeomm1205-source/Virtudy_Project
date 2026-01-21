<template>
  <div>
    <p>로그인 처리 중입니다. 잠시만 기다려주세요...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import api from '@/api';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

onMounted(async () => {
  // 1. URL에서 인가 코드(code) 추출
  const code = route.query.code;

  if (code) {
    try {
      // 2. 백엔드에 인가 코드를 보내 토큰 및 로그인 정보 요청
      const response = await api.post('/auth/kakao', { code });
      
      // 3. 응답에서 Access Token 및 신규/기존 유저 여부 추출
      const { isNewUser, accessToken, signupInfo } = response.data;  

      
      if (isNewUser) {
        // 4-1. 신규 유저 -> 임시 가입 정보를 스토어에 저장                                                                        
        authStore.setSignupInfo(signupInfo); 
        alert('Virtudy에 오신 것을 환영합니다! 몇 가지 추가 정보만 입력해주세요.');                                                
       router.push('/terms');  
      } else {
        // 4-2. 기존 유저 -> Access Token을 스토어에 저장                                                                          
        authStore.setToken(accessToken);                                                                                           
        router.push('/'); 
      }
    } catch (error) {
      console.error('카카오 로그인 처리 실패:', error);
      alert('로그인에 실패했습니다. 문제가 지속되면 관리자에게 문의하세요.');
      router.push('/login');
    }
  } else {
    // 인가 코드가 없는 경우
    alert('비정상적인 접근입니다. 다시 로그인해주세요.');
    router.push('/login');
  }
});
</script>
