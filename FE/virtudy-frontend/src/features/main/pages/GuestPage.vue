<template>
  <div>
    <h1>Virtudy 로그인</h1>
    <button @click="kakaoLogin">
      카카오로 로그인하기
    </button>
  </div>
</template>

<script setup lang="ts">
const kakaoLogin = () => {
  const clientId = import.meta.env.VITE_KAKAO_CLIENT_ID;
  const redirectUri = import.meta.env.VITE_KAKAO_REDIRECT_URI;
  const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}`;
  
  // 카카오 인증 페이지로 리다이렉트
  window.location.href = kakaoAuthUrl;
};
</script>

<style scoped>
div {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  text-align: center;
}
button {
  border: none;
  background: none;
  cursor: pointer;
  padding: 0;
}
</style>