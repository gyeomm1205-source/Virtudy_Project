<template>
  <GlobalNavBar v-if="showGlobalNav" />
  <RouterView />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { RouterView, useRoute } from 'vue-router';
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';

const route = useRoute();
const showGlobalNav = computed(() => !route.meta?.hideGlobalNav);
</script>

<style>
/* 전역 스타일 리셋 등 */
body {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
