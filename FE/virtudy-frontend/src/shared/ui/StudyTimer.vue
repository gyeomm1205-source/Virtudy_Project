<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';

// 공부 시간(초 단위) 상태
const totalSeconds = ref(0);
let timerInterval: ReturnType<typeof setInterval> | null = null;

// 초 -> HH:MM:SS 형식으로 변환하는 계산 속성
const formattedTime = computed(() => {
  const h = Math.floor(totalSeconds.value / 3600);
  const m = Math.floor((totalSeconds.value % 3600) / 60);
  const s = totalSeconds.value % 60;

  // 01:05:09 처럼 두 자리수 맞추기
  const pad = (num: number) => String(num).padStart(2, '0');
  
  // 항상 HH:MM:SS 형식으로 표시
  return `${pad(h)}:${pad(m)}:${pad(s)}`;
});

// 컴포넌트가 켜지면 타이머 시작
onMounted(() => {
  timerInterval = setInterval(() => {
    totalSeconds.value++;
  }, 1000);
});

// 컴포넌트가 꺼지면(방 나가기 등) 타이머 정지 (메모리 누수 방지)
onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
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
  margin-left: 20px;
  transform: scaleY(1.3);
}
</style>