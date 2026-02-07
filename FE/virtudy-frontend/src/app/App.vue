<template>
  <GlobalNavBar v-if="showGlobalNav" />
  <RouterView />

  <AlertModal
    v-if="uiStore.isAlertOpen"
    :title="uiStore.alertTitle"
    :message="uiStore.alertMessage"
    @close="uiStore.closeAlert"
    @confirm="uiStore.handleAlertConfirm"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { RouterView, useRoute } from 'vue-router';
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import { useUiStore } from '@/stores/uiStore';
import AlertModal from '@/shared/ui/AlertModal.vue';

const route = useRoute();
const showGlobalNav = computed(() => !route.meta?.hideGlobalNav);
const uiStore = useUiStore();
</script>

<style>
/* 전역 스타일 리셋 등 */
body {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
