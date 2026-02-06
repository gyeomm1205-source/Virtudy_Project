<template>
  <div class="flex justify-center items-center">
    <CrownIcon 
      class="w-full h-full"
      :style="{ color: tierColor }"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import CrownIcon from '@/assets/icons/crown.svg?component';

interface Props {
  tier?: string;
}

const props = withDefaults(defineProps<Props>(), {
  tier: '',
});

// 티어별 색상 계산 로직 (여기로 통합!)
const tierColor = computed(() => {
  const tierName = props.tier?.toUpperCase() || '';

  if (tierName.includes('PLATINUM')) {
    return '#C4D9E0';
  }
  if (tierName.includes('DIA')) { // DIAMOND 포함
    return '#F5DACD';
  }
  if (tierName.includes('GOLD')) {
    return '#FFD966';
  }
  if (tierName.includes('SILVER')) {
    return '#DCDCDC';
  }
  if (tierName.includes('BRONZE')) {
    return 'C18746';
  }
  
  return 'var(--color-choco)'; // 기본값
});
</script>