<template>
  <div class="bg-[var(--color-cream)] flex flex-col items-center justify-center h-[23.6875rem] p-[1.25rem] rounded-[1.875rem] relative shadow-md">
    <div class="h-[20.8125rem] w-[29.25rem] relative">
      <div class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-[29.25rem] h-[20.8125rem]">
        <svg viewBox="0 0 468 333" class="w-full h-full">
          <!-- 배경 라인들 -->
          <image x="94.632" y="27.132" width="278.736" height="278.736" href="http://localhost:3845/assets/bef8bd2f915d200972958c31992fcafbcb35d61c.svg" />
          <!-- 데이터 폴리곤 (동적) -->
          <image x="153.166" y="166" stroke-width="3" width="161.667" height="111.494" href="http://localhost:3845/assets/f93ddba6c9bcc31ea8f652c30e902315bc50e2d0.svg" />
          <polygon :points="dynamicPoints" fill="var(--color-butter)" opacity="0.7" style="transition: all 0.5s ease-in-out" />
        </svg>
      </div>
      
      <p class="absolute left-1/2 -top-3 -translate-x-1/2 text-[var(--color-syrup)] text-[1.744rem] font-['PfStardust30S'] tracking-[-0.07rem]">지구력</p>
      <p class="absolute right-8 top-25 text-[var(--color-syrup)] text-[1.744rem] font-['PfStardust30S'] tracking-[-0.07rem]">집중력</p>
      <p class="absolute left-[calc(50%+6rem)] bottom-5 text-[var(--color-syrup)] text-[1.744rem] font-['PfStardust30S'] tracking-[-0.07rem] text-center">규칙성</p>
      <p class="absolute right-[calc(50%+6rem)] bottom-5 text-[var(--color-syrup)] text-[1.744rem] font-['PfStardust30S'] tracking-[-0.07rem] text-center">안정감</p>
      <p class="absolute left-8 top-25 text-[var(--color-syrup)] text-[1.744rem] font-['PfStardust30S'] tracking-[-0.07rem] text-right">의지력</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface PentagonProps {
  endurance: number;  // 지구력 (Top)
  focusDepth: number; // 집중력 (Right-Top)
  regularity: number; // 규칙성 (Right-Bottom)
  stability: number;  // 안정감 (Left-Bottom)
  willPower: number;  // 의지력 (Left-Top)
}

const props = withDefaults(defineProps<PentagonProps>(), {
  endurance: 50,
  focusDepth: 50,
  regularity: 50,
  stability: 50,
  willPower: 50
});

// 차트 중심점 (새로운 viewBox 0 0 468 333 기준)
const CENTER = { x: 234, y: 166.5 };

// 각 꼭짓점의 최대 좌표 (값 100일 때) - 정오각형
const MAX_POINTS = [
  { x: 234, y: 27.132 },    // Top (지구력)
  { x: 373.368, y: 113.632 }, // Right-Top (집중력) 
  { x: 337.632, y: 300.265 }, // Right-Bottom (규칙성)
  { x: 130.368, y: 300.265 }, // Left-Bottom (안정감)
  { x: 94.632, y: 113.632 }   // Left-Top (의지력)
];

// 값을 좌표로 변환하는 함수
const getPoint = (value: number, index: number) => {
  const max = MAX_POINTS[index];
  if (!max) {
    return `${CENTER.x},${CENTER.y}`;
  }
  // 벡터 계산: 중심에서 최대점까지의 벡터 * (값 / 100)
  const ratio = Math.min(Math.max(value, 0), 100) / 100; // 0~1 사이 값
  
  const x = CENTER.x + (max.x - CENTER.x) * ratio;
  const y = CENTER.y + (max.y - CENTER.y) * ratio;
  return `${x},${y}`;
};

const dynamicPoints = computed(() => {
  const p1 = getPoint(props.endurance, 0);
  const p2 = getPoint(props.focusDepth, 1);
  const p3 = getPoint(props.regularity, 2);
  const p4 = getPoint(props.stability, 3);
  const p5 = getPoint(props.willPower, 4);
  return `${p1} ${p2} ${p3} ${p4} ${p5}`;
});
</script>