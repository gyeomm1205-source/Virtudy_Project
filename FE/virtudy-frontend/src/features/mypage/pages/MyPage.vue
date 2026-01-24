<template>
  <div>
    <h1>마이페이지</h1>
    <p v-if="authStore.userInfo">안녕하세요, {{ authStore.userInfo.nickName }}님!</p>
    <p>여기는 마이페이지입니다.</p>
    <!-- 여기에 사용자 정보 및 기타 마이페이지 콘텐츠를 추가합니다. -->
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '../../../stores/authStore';
import { onMounted } from 'vue'; // <--- import 필수

const authStore = useAuthStore();
// [핵심] 페이지가 켜질 때 백엔드에 요청해서 유저 정보를 가져옵니다.
onMounted(async () => {
  // 정보가 비어있으면 가져오라!
  if (!authStore.userInfo) {
    await authStore.fetchUserInfo();
  }
});
</script>

<style scoped>
/* 마이페이지 스타일을 여기에 추가합니다. */
div {
  padding: 20px;
}
</style>
