<template>
  <div class="bg-[var(--color-cream)] flex flex-col items-center justify-center h-[23.6875rem] p-[1.25rem] rounded-[1.875rem] relative shadow-md">
    
    <div class="w-full h-full relative">
      <Radar 
        :data="chartData" 
        :options="chartOptions" 
        class="w-full h-full" 
      />
      
      <p :class="`absolute ${layoutConfig.endurance} text-[var(--color-syrup)] font-['PfStardust30S']`">
        지구력
      </p>
      
      <p :class="`absolute ${layoutConfig.focusDepth} text-[var(--color-syrup)] font-['PfStardust30S']`">
        집중력
      </p>
      
      <p :class="`absolute ${layoutConfig.regularity} text-[var(--color-syrup)] font-['PfStardust30S']`">
        규칙성
      </p>
      
      <p :class="`absolute ${layoutConfig.stability} text-[var(--color-syrup)] font-['PfStardust30S']`">
        안정감
      </p>
      
      <p :class="`absolute ${layoutConfig.willPower} text-[var(--color-syrup)] font-['PfStardust30S']`">
        의지력
      </p>
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

//레이아웃 설정을 위한 인터페이스 정의
interface LayoutConfig {
  endurance?: string;
  focusDepth?: string;
  regularity?: string;
  stability?: string;
  willPower?: string;
}

interface PentagonProps {
  endurance: number;
  focusDepth: number;
  regularity: number;
  stability: number;
  willPower: number;
  layout?: LayoutConfig;
}

const props = withDefaults(defineProps<PentagonProps>(), {
  endurance: 50,
  focusDepth: 50,
  regularity: 50,
  stability: 50,
  willPower: 50,
  // 기본값을 현재 MyPage에서 예쁘게 보이는 값으로 설정 (기존 코드 값 유지)
  layout: () => ({})
});

// 들어온 layout prop과 기본값을 병합
// 기본값에 폰트 크기와 위치 세부 조정(-translate 등)을 모두 포함시킵니다.
const layoutConfig = computed(() => ({
  endurance: props.layout?.endurance || 'top-[-4%] left-1/2 -translate-x-1/2 text-[2rem]',
  focusDepth: props.layout?.focusDepth || 'top-[30%] right-25 text-[2rem]',
  regularity: props.layout?.regularity || 'bottom-[1%] right-41 text-[2rem]',
  stability: props.layout?.stability || 'bottom-[1%] left-41 text-[2rem]',
  willPower: props.layout?.willPower || 'top-[30%] left-25 text-[2rem]'
}));

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

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  layout: {
    padding: {
      top: 30,
      bottom: 30,
      left: 30,
      right: 30
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