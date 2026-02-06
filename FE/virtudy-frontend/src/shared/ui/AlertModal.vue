<template>
  <div class="fixed inset-0 bg-[rgba(255,253,245,0.65)] backdrop-blur-md flex items-center justify-center z-[9999]">
    <div class="bg-[var(--color-cream2)] p-[2.5rem] rounded-[0.75rem] shadow-[4px_4px_0px_0px_var(--color-choco)] relative max-w-[26rem] w-full mx-[1rem]">
      
      <button 
        @click="onClose"
        class="absolute top-[1rem] right-[1rem] w-[2.625rem] h-[2.625rem] cursor-pointer hover:scale-110 transition-transform flex items-center justify-center"
      >
        <div class="w-[1.5rem] h-[1.5rem] relative">
          <div class="absolute top-[4px] left-[4px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[8px] left-[8px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[11px] left-[11px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[14px] left-[14px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[18px] left-[18px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          
          <div class="absolute top-[4px] left-[18px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[8px] left-[14px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[11px] left-[11px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[14px] left-[8px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[18px] left-[4px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
        </div>
      </button>

      <div class="flex flex-col items-center gap-[1.5rem]">
        <div class="text-[var(--color-choco)] text-[2rem] font-['Xcu'] font-medium leading-none mt-2">
          {{ title }}
        </div>

        <div class="text-[var(--color-syrup)] text-[1.125rem] font-['PfStardust30S'] text-center leading-relaxed whitespace-pre-wrap">
          {{ message }}
        </div>

        <button
          @click="onConfirm"
          class="mt-2 bg-[var(--color-butter)] border-2 border-[var(--color-choco)] rounded-[0.5rem] px-[2rem] py-[0.75rem] text-[var(--color-choco)] font-['PfStardust30S'] text-[1.25rem] shadow-[2px_2px_0px_0px_var(--color-choco)] hover:scale-105 active:scale-95 active:shadow-none active:translate-x-[2px] active:translate-y-[2px] transition-all"
        >
          확인
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  title?: string;
  message: string;
}>(), {
  title: '알림', // 기본 제목
  message: ''
});

const emit = defineEmits(['close', 'confirm']);

const onClose = () => {
  emit('close');
};

const onConfirm = () => {
  emit('confirm');
  emit('close'); // 확인 버튼 눌러도 닫히도록 설정
};
</script>