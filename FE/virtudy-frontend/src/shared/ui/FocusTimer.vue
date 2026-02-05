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
  // 항상 HH:MM:SS 형식으로 표시
  return `${pad(h)}:${pad(m)}:${pad(s)}`;
});
</script>

<template>
  <div class="study-timer">
    <span class="time">{{ formattedTime }}</span>
  </div>
</template>

<style scoped>
.study-timer {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
}

.time {
  font-family: 'exqt', sans-serif;
  font-size: 4.5rem;
  color: #ffd966;
  font-weight: 100;
  line-height: 1;
  letter-spacing: -2px;
  white-space: nowrap;
  text-align: center;
  margin-top: 10px;
  margin-left: 36px;
  transform: scaleY(1.3);
}
</style>
