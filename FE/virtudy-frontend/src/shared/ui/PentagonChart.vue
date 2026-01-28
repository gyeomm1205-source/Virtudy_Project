<template>
  <div class="bg-[var(--color-cream)] flex flex-col items-center justify-center h-[23.6875rem] p-[1.25rem] rounded-[1.875rem] relative shadow-md">
    <div class="h-[20.8125rem] w-[29.25rem] relative">
      <div class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-[22rem] h-[22rem]">
        <svg viewBox="45 0 190 190" class="w-full h-full">
          <polygon points="139.5,20 213.5,86 185.5,172 93.5,172 65.5,86" fill="none" stroke="var(--color-syrup)" stroke-width="2" opacity="0.3"/>
          <polygon points="139.5,40 193.5,96 175.5,152 103.5,152 85.5,96" fill="none" stroke="var(--color-syrup)" stroke-width="2" opacity="0.3"/>
          <polygon points="139.5,60 173.5,106 165.5,132 113.5,132 105.5,106" fill="none" stroke="var(--color-syrup)" stroke-width="2" opacity="0.3"/>
          <polygon points="139.5,80 153.5,116 155.5,112 123.5,112 125.5,116" fill="none" stroke="var(--color-syrup)" stroke-width="2" opacity="0.3"/>
          
          <polygon :points="dynamicPoints" fill="var(--color-butter)" stroke="var(--color-choco)" stroke-width="3" opacity="0.8"/>
          
          <line x1="139.5" y1="139.5" x2="139.5" y2="20" stroke="var(--color-syrup)" stroke-width="1" opacity="0.5"/>
          <line x1="139.5" y1="139.5" x2="213.5" y2="86" stroke="var(--color-syrup)" stroke-width="1" opacity="0.5"/>
          <line x1="139.5" y1="139.5" x2="185.5" y2="172" stroke="var(--color-syrup)" stroke-width="1" opacity="0.5"/>
          <line x1="139.5" y1="139.5" x2="93.5" y2="172" stroke="var(--color-syrup)" stroke-width="1" opacity="0.5"/>
          <line x1="139.5" y1="139.5" x2="65.5" y2="86" stroke="var(--color-syrup)" stroke-width="1" opacity="0.5"/>
        </svg>
      </div>
      
      <p class="absolute left-1/2 top-0 -translate-x-1/2 text-[var(--color-syrup)] text-[1.5rem] font-['PfStardust30S']">지구력</p>
      <p class="absolute right-8 top-[calc(50%-2.3rem)] text-[var(--color-syrup)] text-[1.5rem] font-['PfStardust30S']">집중력</p>
      <p class="absolute right-[5rem] bottom-[1rem] text-[var(--color-syrup)] text-[1.5rem] font-['PfStardust30S']">규칙성</p>
      <p class="absolute left-[5rem] bottom-[1rem] text-[var(--color-syrup)] text-[1.5rem] font-['PfStardust30S']">안정감</p>
      <p class="absolute left-8 top-[calc(50%-2.3rem)] text-[var(--color-syrup)] text-[1.5rem] font-['PfStardust30S']">의지력</p>
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

// 차트 중심점 (SVG 좌표 기준, 눈대중으로 맞춘 중심)
const CENTER = { x: 139.5, y: 139.5 }; // *주의: 제공된 SVG 그리드가 정오각형이 아니라 중심이 약간 아래에 있습니다. 
// 시각적 중심을 맞추기 위해 Y값을 그리드 중심(대략 110 정도)에 맞춰야 정확하지만, 
// 여기서는 제공된 SVG의 line x1, y1 시작점인 139.5를 따랐습니다. 
// 만약 그리드와 어긋나면 CENTER.y 값을 105~110 사이로 조정하세요.

// 각 꼭짓점의 최대 좌표 (값 100일 때)
const MAX_POINTS = [
  { x: 139.5, y: 20 },  // Top (지구력)
  { x: 213.5, y: 86 },  // Right-Top (집중력)
  { x: 185.5, y: 172 }, // Right-Bottom (규칙성)
  { x: 93.5, y: 172 },  // Left-Bottom (안정감)
  { x: 65.5, y: 86 }    // Left-Top (의지력)
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