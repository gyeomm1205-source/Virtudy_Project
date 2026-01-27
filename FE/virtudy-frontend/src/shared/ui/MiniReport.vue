<template>
  <div class="study-stats-container">
    <div class="stat-item">
      <span class="value">{{ formatTime(studyTime) }}</span>
      <span class="label">공부시간</span>
    </div>
    <div class="stat-item">
      <span class="value">{{ focusing }}%</span>
      <span class="label">집중도</span>
    </div>
  </div>
</template>

<script setup lang="ts">
// [수정] interface를 정의하고 withDefaults를 사용해 기본값을 지정
interface Props {
  studyTime?: number;  // [핵심] 물음표(?)를 붙여서 없어도 된다고 표시!
  focusing?: number;   // [핵심] 물음표(?) 추가
}

// 기본값도 숫자 0으로 설정
const props = withDefaults(defineProps<Props>(), {
  studyTime: 0,
  focusing: 0
});

// [추가] 숫자를 받아서 "0h 00m" 형태로 바꿔주는 함수
const formatTime = (minutes: number) => {
  if (!minutes) return '0h'; // 값이 없으면 0h
  
  const hours = Math.floor(minutes / 60); // 시간 계산
  const mins = minutes % 60;              // 분 계산

  // 0분이면 시간만 표시 (예: 2h)
  if (mins === 0) return `${hours}h`;
  
  // 분이 있으면 같이 표시 (예: 1h 30m)
  return `${hours}h ${mins}m`;
};

</script>
