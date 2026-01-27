<template>
  <div class="bg-[var(--color-cream)] flex flex-col items-center justify-center h-[23.6875rem] p-[1.25rem] rounded-[1.875rem] relative">
    <div class="h-[20.8125rem] w-[29.25rem] relative">
      <!-- 오각형 그리드 라인들 -->
      <div class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-[22rem] h-[22rem]">
        <svg viewBox="45 0 190 190" class="w-full h-full">
          <!-- 외곽 오각형들 (그리드 라인) -->
          <polygon points="139.5,20 213.5,86 185.5,172 93.5,172 65.5,86" 
                   fill="none" stroke="var(--color-syrup)" stroke-width="2" opacity="0.3"/>
          <polygon points="139.5,40 193.5,96 175.5,152 103.5,152 85.5,96" 
                   fill="none" stroke="var(--color-syrup)" stroke-width="2" opacity="0.3"/>
          <polygon points="139.5,60 173.5,106 165.5,132 113.5,132 105.5,106" 
                   fill="none" stroke="var(--color-syrup)" stroke-width="2" opacity="0.3"/>
          <polygon points="139.5,80 153.5,116 155.5,112 123.5,112 125.5,116" 
                   fill="none" stroke="var(--color-syrup)" stroke-width="2" opacity="0.3"/>
          <polygon points="139.5,100 143.5,126 145.5,102 133.5,102 135.5,126" 
                   fill="none" stroke="var(--color-syrup)" stroke-width="2" opacity="0.3"/>
          
          <!-- 데이터 오각형 (실제 값) -->
          <polygon points="139.5,50 183.5,100 170.5,145 108.5,145 95.5,100" 
                   fill="var(--color-butter)" stroke="var(--color-choco)" stroke-width="2" opacity="0.8"/>
          
          <!-- 중심에서 각 꼭짓점으로의 선 -->
          <line x1="139.5" y1="139.5" x2="139.5" y2="20" stroke="var(--color-syrup)" stroke-width="1" opacity="0.5"/>
          <line x1="139.5" y1="139.5" x2="213.5" y2="86" stroke="var(--color-syrup)" stroke-width="1" opacity="0.5"/>
          <line x1="139.5" y1="139.5" x2="185.5" y2="172" stroke="var(--color-syrup)" stroke-width="1" opacity="0.5"/>
          <line x1="139.5" y1="139.5" x2="93.5" y2="172" stroke="var(--color-syrup)" stroke-width="1" opacity="0.5"/>
          <line x1="139.5" y1="139.5" x2="65.5" y2="86" stroke="var(--color-syrup)" stroke-width="1" opacity="0.5"/>
        </svg>
      </div>
      
      <!-- 라벨들 -->
      <p class="absolute left-1/2 top-0 -translate-x-1/2 text-[var(--color-syrup)] text-[1.742rem] font-['PfStardust30S'] font-normal leading-none tracking-[-0.0697rem] text-center">
        지구력
      </p>
      <p class="absolute right-8 top-[calc(50%-2.3rem)] text-[var(--color-syrup)] text-[1.742rem] font-['PfStardust30S'] font-normal leading-none tracking-[-0.0697rem]">
        집중력
      </p>
      <p class="absolute right-[5rem] bottom-[1rem] text-[var(--color-syrup)] text-[1.742rem] font-['PfStardust30S'] font-normal leading-none tracking-[-0.0697rem] text-center">
        규칙성
      </p>
      <p class="absolute left-[5rem] bottom-[1rem] text-[var(--color-syrup)] text-[1.742rem] font-['PfStardust30S'] font-normal leading-none tracking-[-0.0697rem] text-center">
        안정감
      </p>
      <p class="absolute left-8 top-[calc(50%-2.3rem)] text-[var(--color-syrup)] text-[1.742rem] font-['PfStardust30S'] font-normal leading-none tracking-[-0.0697rem] text-right">
        의지력
      </p>
    </div>
  </div>
</template>
