<template>
  <div>
    <UserPage v-if="authStore.isLoggedIn" />
    <GuestPage v-else />
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/authStore';
import GuestPage from './GuestPage.vue';
import UserPage from './UserPage.vue';

const authStore = useAuthStore();
</script>