<template>
  <div class="fixed inset-0 bg-black/20 backdrop-blur-sm flex items-center justify-center z-50">
    <div class="bg-[var(--color-cream2)] p-[2.5rem] rounded-[0.75rem] shadow-[4px_4px_0px_0px_var(--color-choco)] relative max-w-[26rem] w-full mx-[1rem]">
      <button 
        @click="$emit('close')"
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
        <div class="text-[var(--color-choco)] text-[2rem] font-['Xcu'] font-medium leading-none">
          매칭 중...
        </div>
        <div class="text-[var(--color-syrup)] text-[1.125rem] font-['PfStardust30S'] text-center leading-snug">
          잠시만 기다려 주세요!
        </div>
        <div class="flex items-center gap-2">
          <span class="w-2 h-2 rounded-full bg-[var(--color-choco)] animate-bounce"></span>
          <span class="w-2 h-2 rounded-full bg-[var(--color-choco)] animate-bounce [animation-delay:150ms]"></span>
          <span class="w-2 h-2 rounded-full bg-[var(--color-choco)] animate-bounce [animation-delay:300ms]"></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineEmits(['close']);
</script>
