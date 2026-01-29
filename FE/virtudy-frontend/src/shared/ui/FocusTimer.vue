<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  seconds: number;
}

const props = defineProps<Props>();

const formattedTime = computed(() => {
  const h = Math.floor(props.seconds / 3600);
  const m = Math.floor((props.seconds % 3600) / 60);
  const s = props.seconds % 60;

  const pad = (num: number) => String(num).padStart(2, '0');
  if (h === 0) return `${pad(m)}:${pad(s)}`;
  return `${pad(h)}:${pad(m)}:${pad(s)}`;
});
</script>

<template>
  <div class="focus-timer">
    <span class="icon">🎯</span>
    <span class="label">집중 타이머</span>
    <span class="time">{{ formattedTime }}</span>
  </div>
</template>

<style scoped>
.focus-timer {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: rgba(0, 0, 0, 0.1);
  padding: 8px 16px;
  border-radius: 20px;
  font-family: 'Courier New', Courier, monospace;
  font-weight: bold;
  color: #2f3542;
  border: 1px solid #ddd;
}

.label {
  font-size: 0.9rem;
  font-weight: 600;
}

.time {
  font-size: 1.2rem;
  min-width: 80px;
  text-align: center;
}
</style>
