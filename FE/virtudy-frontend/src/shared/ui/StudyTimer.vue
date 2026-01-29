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
  
  // 1시간 미만일 때는 MM:SS만 보여주기 (선택사항)
  if (h === 0) return `${pad(m)}:${pad(s)}`;
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
    <span class="icon">⏱️</span>
    <span class="time">{{ formattedTime }}</span>
  </div>
</template>

<style scoped>
.study-timer {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: rgba(0, 0, 0, 0.1); /* 배경 살짝 어둡게 */
  padding: 8px 16px;
  border-radius: 20px;
  font-family: 'Courier New', Courier, monospace; /* 숫자 간격 일정한 폰트 추천 */
  font-weight: bold;
  color: #2f3542;
  border: 1px solid #ddd;
}

.time {
  font-size: 1.2rem;
  min-width: 80px; /* 시간 글자수 바껴도 흔들림 방지 */
  text-align: center;
}
</style>