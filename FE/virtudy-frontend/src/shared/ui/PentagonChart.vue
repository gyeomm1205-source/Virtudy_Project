<template>
  <div class="bg-[var(--color-cream)] flex flex-col items-center justify-center h-[23.6875rem] p-[1.25rem] rounded-[1.875rem] relative shadow-md">
    
    <div class="w-full h-full relative">
      <Radar 
        :data="chartData" 
        :options="chartOptions" 
        class="w-full h-full" 
      />
      
      <p class="absolute top-[-3%] left-1/2 -translate-x-1/2 text-[var(--color-syrup)] font-['PfStardust30S'] text-[2rem]">지구력</p>
      
      <p class="absolute top-[30%] right-25 text-[var(--color-syrup)] font-['PfStardust30S'] text-[2rem]">집중력</p>
      
      <p class="absolute bottom-[1%] right-41 text-[var(--color-syrup)] font-['PfStardust30S'] text-[2rem]">규칙성</p>
      
      <p class="absolute bottom-[1%] left-41 text-[var(--color-syrup)] font-['PfStardust30S'] text-[2rem]">안정감</p>
      
      <p class="absolute top-[30%] left-25 text-[var(--color-syrup)] font-['PfStardust30S'] text-[2rem]">의지력</p>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Radar } from 'vue-chartjs';
import {
  Chart as ChartJS,
  RadialLinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
  Legend
} from 'chart.js';

ChartJS.register(RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend);

interface PentagonProps {
  endurance: number;
  focusDepth: number;
  regularity: number;
  stability: number;
  willPower: number;
}

const props = withDefaults(defineProps<PentagonProps>(), {
  endurance: 50,
  focusDepth: 50,
  regularity: 50,
  stability: 50,
  willPower: 50
});

const chartData = computed(() => ({
  labels: ['지구력', '집중력', '규칙성', '안정감', '의지력'],
  datasets: [
    {
      label: '능력치',
      data: [
        props.endurance,
        props.focusDepth,
        props.regularity,
        props.stability,
        props.willPower
      ],
      backgroundColor: 'rgba(255, 230, 109, 0.7)',
      borderColor: 'transparent',
      borderWidth: 0,
      pointRadius: 0,
      pointHoverRadius: 0,
    }
  ]
}));

// hideCenterPointPlugin 코드는 삭제했습니다.

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  layout: {
    padding: {
      top: 30,    // 위쪽 여백을 줌 → 차트가 [아래]로 내려감
      bottom: 30,  // 아래쪽 여백
      left: 30,    // 왼쪽 여백을 줌 → 차트가 [오른쪽]으로 이동
      right: 30    // 오른쪽 여백
    }
  },
  plugins: {
    legend: { display: false },
    tooltip: { enabled: true },
  },
  scales: {
    r: {
      min: 0,
      max: 100,
      ticks: {
        stepSize: 33,
        display: false
      },
      pointLabels: {
        display: false
      },
      grid: {
        color: '#805143',
        lineWidth: 1
      },
      angleLines: {
        color: '#805143',
        lineWidth: 1.5
      }
    }
  }
};
</script>