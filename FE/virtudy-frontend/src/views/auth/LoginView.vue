<template>
  <div class="login-container">
    <h1>Virtudy 로그인</h1>
    <button @click="loginWithKakao" style="background-color: #FEE500; border: none; padding: 10px 20px; cursor: pointer;">
      카카오로 시작하기
    </button>
  </div>
</template>

<script setup>
const loginWithKakao = () => {
  //카카오에서 발급받은 REST API 키 (백엔드와 동일해야 함) 환경 변수에서 불러오기
  const CLIENT_ID = import.meta.env.VITE_KAKAO_CLIENT_ID;
  //카카오 로그인 후 돌아올 우리 사이트 주소 (백엔드에 등록된 Redirect URI로 나중에 수정해야될 수도 있음)
  const REDIRECT_URI = import.meta.env.VITE_KAKAO_REDIRECT_URI;
  
  //카카오 인증 페이지 URL 구성
  const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code`;

  //카카오 로그인 페이지로 이동
  window.location.href = KAKAO_AUTH_URL;
};
</script>