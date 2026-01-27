<template>
  <div class="bg-[var(--color-syrup)] border-2 border-[var(--color-choco)] border-solid h-full w-full overflow-clip" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
    
    <div v-if="isLoading" class="absolute inset-0 flex items-center justify-center bg-[var(--color-syrup)] z-10">
      <p class="text-[var(--color-choco)] text-xl font-['PfStardust30S']">랭킹 로딩중...</p>
    </div>

    <div class="relative h-[19.625rem] overflow-clip">
      <div class="absolute left-[1.688rem] top-[2.188rem] transform -translate-y-1/2">
        <p class="text-[var(--color-choco)] text-[1.75rem] font-['Xcu'] font-normal leading-normal">
          개인 랭킹
        </p>
      </div>
      
      <div class="absolute left-1/2 top-[calc(50%+1.594rem)] transform -translate-x-1/2 -translate-y-1/2 w-[26.938rem] h-[15.688rem] flex flex-col rounded-[1.25rem] overflow-hidden">
        
        <div v-if="!displayedPrivateRanking.length" class="flex-1 flex items-center justify-center bg-[var(--color-cream)]">
          <span class="text-[var(--color-pancake)] font-['PfStardust30S']">데이터가 없습니다.</span>
        </div>

        <div 
          v-else
          v-for="(item, index) in displayedPrivateRanking" 
          :key="`personal-${index}`"
          class="flex-1 relative w-full"
          :class="index % 2 === 0 ? 'bg-[var(--color-choco)]' : 'bg-[var(--color-cream)]'"
        >
          <div class="absolute left-[1.688rem] top-1/2 transform -translate-y-1/2 w-[1.938rem]">
            <p 
              class="text-[1.75rem] font-['Xcu'] font-normal leading-normal"
              :class="index % 2 === 0 ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.rank }}
            </p>
          </div>
          
          <div class="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[12.625rem]">
            <p 
              class="text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center truncate"
              :class="index === 0 ? 'text-[var(--color-butter)]' : 
                     index % 2 === 0 ? 'text-[var(--color-pancake)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.nickName }}
            </p>
          </div>
          
          <div class="absolute left-[19.969rem] top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[6.063rem]">
            <p 
              class="text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center"
              :class="index === 0 ? 'text-[var(--color-butter)]' : 
                     index % 2 === 0 ? 'text-[var(--color-pancake)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.score }}p
            </p>
          </div>
          
          <div class="absolute right-[1.563rem] top-1/2 transform -translate-y-1/2 w-[1.5rem] h-[1.5rem]">
            <svg viewBox="0 0 24 24" class="w-full h-full">
              <path 
                d="M5 16L3 8L9 10L12 6L15 10L21 8L19 16H5Z" 
                :fill="index === 0 ? 'var(--color-butter)' : 'var(--color-syrup)'"
                stroke="var(--color-choco)" 
                stroke-width="1"
              />
            </svg>
          </div>
        </div>
      </div>
    </div>
    
    <div class="relative h-[19.625rem] overflow-clip">
      <div class="absolute left-[1.688rem] top-[1.313rem] transform -translate-y-1/2">
        <p class="text-[var(--color-choco)] text-[1.75rem] font-['Xcu'] font-normal leading-normal">
          팀 랭킹
        </p>
      </div>
      
      <div class="absolute left-1/2 top-[calc(50%+0.719rem)] transform -translate-x-1/2 -translate-y-1/2 w-[26.938rem] h-[15.688rem] flex flex-col rounded-[1.25rem] overflow-hidden">
        
        <div v-if="!displayedTeamRanking.length" class="flex-1 flex items-center justify-center bg-[var(--color-cream)]">
          <span class="text-[var(--color-pancake)] font-['PfStardust30S']">데이터가 없습니다.</span>
        </div>

        <div 
          v-else
          v-for="(item, index) in displayedTeamRanking" 
          :key="`team-${index}`"
          class="flex-1 relative w-full"
          :class="index % 2 === 0 ? 'bg-[var(--color-choco)]' : 'bg-[var(--color-cream)]'"
        >
          <div class="absolute left-[1.688rem] top-1/2 transform -translate-y-1/2 w-[1.938rem]">
            <p 
              class="text-[1.75rem] font-['Xcu'] font-normal leading-normal"
              :class="index % 2 === 0 ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.rank }}
            </p>
          </div>
          
          <div class="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[12.625rem]">
            <p 
              class="text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center truncate"
              :class="index === 0 ? 'text-[var(--color-butter)]' : 
                     index % 2 === 0 ? 'text-[var(--color-pancake)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.nickName }}
            </p>
          </div>
          
          <div class="absolute left-[19.969rem] top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[6.063rem]">
            <p 
              class="text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center"
              :class="index === 0 ? 'text-[var(--color-butter)]' : 
                     index % 2 === 0 ? 'text-[var(--color-pancake)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.score }}p
            </p>
          </div>
          
          <div class="absolute right-[1.563rem] top-1/2 transform -translate-y-1/2 w-[1.5rem] h-[1.5rem]">
            <svg viewBox="0 0 24 24" class="w-full h-full">
              <path 
                d="M5 16L3 8L9 10L12 6L15 10L21 8L19 16H5Z" 
                :fill="index === 0 ? 'var(--color-butter)' : 'var(--color-syrup)'"
                stroke="var(--color-choco)" 
                stroke-width="1"
              />
            </svg>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
// [중요] 실제 백엔드 타입 import
import type { RankItem } from '@/features/ranking/types/ranking.types';

// UserPage에서 넘겨주는 이름(privateTop5, teamTop5)과 일치
interface RankingSectionMiniProps {
  privateTop5?: RankItem[];
  teamTop5?: RankItem[];
  isLoading?: boolean;
}

const props = withDefaults(defineProps<RankingSectionMiniProps>(), {
  privateTop5: () => [], // 더미데이터 제거, 빈 배열로 초기화
  teamTop5: () => [],
  isLoading: false,
});

// 상위 5개만 안전하게 자르기 (혹시 백엔드가 더 많이 줄 경우 대비)
const displayedPrivateRanking = computed(() => {
  return props.privateTop5.slice(0, 5);
});

const displayedTeamRanking = computed(() => {
  return props.teamTop5.slice(0, 5);
});
</script>