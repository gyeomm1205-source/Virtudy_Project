<template>
  <div class="bg-[var(--color-choco)] flex items-center justify-center gap-[12.5rem] h-[9.9375rem] px-[12.75rem] py-[1.125rem] rounded-[1.25rem] overflow-hidden">
    <div class="flex flex-col gap-[0.3125rem] items-start">
      <div class="text-[var(--color-cream2)] text-[2.625rem] font-['Ram'] font-medium leading-[3rem] tracking-[-0.0525rem]">
        {{ formatTime(studyTime) }}
      </div>
      <div class="text-[var(--color-syrup)] text-[1.5rem] font-['PfStardust30S'] font-normal leading-none whitespace-nowrap">
        공부시간
      </div>
    </div>

    <div class="flex flex-col gap-[0.3125rem] items-start">
      <div class="text-[var(--color-cream2)] text-[2.625rem] font-['Ram'] font-medium leading-[3rem] tracking-[-0.0525rem]">
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
  //분이 딱 맞아떨어지면 시간만 표시(120분->2h)
  if (mins === 0) return `${hours}h`;
  //나머지가 있으면 분까지 리턴(135분->2h 15m)
  return `${hours}h ${mins}m`;
};
</script>