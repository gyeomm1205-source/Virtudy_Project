<template>
  <div>
    <p>로그인 처리 중입니다. 잠시만 기다려주세요...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useOAuthCallback } from '../logic/useOAuthCallback';

const { handleOAuthCallback } = useOAuthCallback();

onMounted(() => {
  handleOAuthCallback();
});
</script>