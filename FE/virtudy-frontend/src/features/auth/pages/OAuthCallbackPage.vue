<template>
  <GlobalBackground :skyType="1">
  <div>
    <p>로그인 처리 중입니다. 잠시만 기다려주세요...</p>
  </div>
  </GlobalBackground>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useOAuthCallback } from '../logic/useOAuthCallback';
import GlobalBackground from '@/shared/ui/GlobalBackground.vue';

const { handleOAuthCallback } = useOAuthCallback();

onMounted(() => {
  handleOAuthCallback();
});
</script>