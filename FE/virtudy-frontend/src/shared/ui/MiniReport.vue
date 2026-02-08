<template>
  <div class="study-stats-container bg-[var(--color-choco)] flex items-center justify-center w-full h-full rounded-[1.25rem] overflow-hidden">
    
    <div class="flex flex-col gap-[0.3125rem] items-center justify-center">
      <div class="text-[var(--color-cream2)] text-[2.625rem] font-['Ram'] font-medium leading-[3rem] tracking-[-0.0525rem] whitespace-nowrap">
        {{ formatTime(studyTime) }}
      </div>
      <div class="text-[var(--color-syrup)] text-[1.5rem] font-['PfStardust30S'] font-normal leading-none whitespace-nowrap">
        공부시간
      </div>
    </div>

    <div class="flex flex-col gap-[0.3125rem] items-center justify-center">
      <div class="text-[var(--color-cream2)] text-[2.625rem] font-['Ram'] font-medium leading-[3rem] tracking-[-0.0525rem] whitespace-nowrap">
        {{ focusing }}%
      </div>
      <div class="text-[var(--color-syrup)] text-[1.5rem] font-['PfStardust30S'] font-normal leading-none whitespace-nowrap">
        집중도
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  studyTime?: number;
  focusing?: number;
}

const props = withDefaults(defineProps<Props>(), {
  studyTime: 0,
  focusing: 0
});

const formatTime = (minutes: number) => {
  if (!minutes) return '0h';
  
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  
  if (mins === 0) return `${hours}h`;
  return `${hours}h ${mins}m`;
};
</script>