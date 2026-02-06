<script setup>
import AvatarGenerator from '@/features/avatar/ui/AvatarGenerator.vue';
import GlobalBackground from '@/shared/ui/GlobalBackground.vue';
</script>

<template>
  <GlobalBackground :skyType="1">
  <div class="page-layout">
    <AvatarGenerator />
  </div>
  </GlobalBackground>
</template>
