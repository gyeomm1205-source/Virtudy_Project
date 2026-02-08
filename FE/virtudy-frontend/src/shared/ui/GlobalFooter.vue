<template>
  <footer 
    class="w-full h-[8rem] flex items-center justify-center flex-shrink-0 relative z-50 backdrop-blur-sm"
    style="background-color: color-mix(in srgb, var(--color-choco), transparent 70%);"
  >
    
    <div class="flex flex-col items-center justify-center gap-2">
      
      <div class="flex items-end gap-3">
        <span class="text-white font-['Ram'] text-[1.5rem] leading-none [text-shadow:2px_2px_0px_var(--color-choco)]">
          Virtudy
        </span>
        <span class="text-white font-['PfStardust30S'] text-[1rem] opacity-90 mb-[2px] [text-shadow:1px_1px_0px_var(--color-choco)]">
          Avatar Cam Study Platform
        </span>
      </div>

      <p class="text-white font-['PfStardust30S'] text-[0.875rem] opacity-80 [text-shadow:1px_1px_0px_var(--color-choco)]">
        Copyright © 2026 Team A703. All rights reserved.
      </p>

    </div>

  </footer>
</template>

<style scoped>
/* 혹시 color-mix가 동작하지 않는 구형 브라우저 대비용 (선택 사항)
  대부분의 최신 브라우저에서는 style 태그의 color-mix가 잘 작동합니다.
*/
</style>
