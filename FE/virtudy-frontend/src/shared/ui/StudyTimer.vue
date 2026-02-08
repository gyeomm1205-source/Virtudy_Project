<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  seconds: number;
}

const props = defineProps<Props>();

// 초 -> HH:MM:SS 형식으로 변환
const formattedTime = computed(() => {
  // [수정] totalSeconds.value 대신 props.seconds 사용
  const h = Math.floor(props.seconds / 3600);
  const m = Math.floor((props.seconds % 3600) / 60);
  const s = props.seconds % 60;

  const pad = (num: number) => String(num).padStart(2, '0');
  
  return `${pad(h)}:${pad(m)}:${pad(s)}`;
});

</script>

<template>
  <div class="focus-timer">
    <span class="time">{{ formattedTime }}</span>
  </div>
</template>

<style scoped>
.focus-timer {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-bottom: 30px;
}

.time {
  font-family: 'Xcu', sans-serif;
  font-size: 37.497px;
  color: #dfa67b;
  font-weight: 500;
  line-height: normal;
  white-space: nowrap;
  text-align: center;
  margin-left: 173px;
  transform: scaleY(1.3);
}
</style>