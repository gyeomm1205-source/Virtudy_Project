<template>
  <GlobalBackground :skyType="1">
    <div class="fixed inset-0 bg-[rgba(255,253,245,0.65)] backdrop-blur-md flex items-center justify-center z-50">
      <div class="bg-[var(--color-cream2)] p-[2.5rem] rounded-[0.75rem] shadow-[4px_4px_0px_0px_var(--color-choco)] relative max-w-[26rem] w-full mx-[1rem]">
        
        <div class="flex flex-col items-center gap-[1.5rem]">
          <div class="text-[var(--color-choco)] text-[2rem] font-['Xcu'] font-medium leading-none">
            로그인 중...
          </div>
          
          <div class="text-[var(--color-syrup)] text-[1.125rem] font-['PfStardust30S'] text-center leading-snug">
            잠시만 기다려주세요...
          </div>
          
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-[var(--color-choco)] animate-bounce"></span>
            <span class="w-2 h-2 rounded-full bg-[var(--color-choco)] animate-bounce [animation-delay:150ms]"></span>
            <span class="w-2 h-2 rounded-full bg-[var(--color-choco)] animate-bounce [animation-delay:300ms]"></span>
          </div>
        </div>
      </div>
    </div>
  </GlobalBackground>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useOAuthCallback } from '../logic/useOAuthCallback';
import GlobalBackground from '@/shared/ui/GlobalBackground.vue';

const { handleOAuthCallback } = useOAuthCallback();

onMounted(() => {
  handleOAuthCallback();
});
</script>