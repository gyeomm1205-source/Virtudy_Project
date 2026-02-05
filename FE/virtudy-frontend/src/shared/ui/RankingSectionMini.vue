<template>
  <div
    class="bg-[var(--color-syrup)] border-2 border-[var(--color-choco)] border-solid h-full w-full overflow-clip cursor-pointer"
    style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
    role="button"
    tabindex="0"
    @click="goToRanking"
    @keyup.enter="goToRanking"
  >
    
    <div class="relative h-[19.625rem] overflow-clip">
      <div class="absolute left-[1.688rem] top-[2.188rem] transform -translate-y-1/2">
        <p class="text-[var(--color-choco)] text-[1.75rem] font-['Xcu'] font-normal leading-normal">
          개인 랭킹
        </p>
      </div>
      
      <div class="absolute left-1/2 top-[calc(50%+1.594rem)] transform -translate-x-1/2 -translate-y-1/2 w-[26.938rem] h-[15.688rem] flex flex-col rounded-[1.25rem] overflow-hidden">
        
        <div v-if="isLoading" class="flex-1 flex items-center justify-center bg-[var(--color-cream)] text-[var(--color-choco)] font-['PfStardust30S'] text-xl">
           불러오는 중...
        </div>

        <div 
          v-else
          v-for="(item, index) in displayedPrivateRanking" 
          :key="`private-${index}`"
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
          
          <div class="absolute left-45 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[11rem]">
            <p 
              class="text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center truncate"
              :class="index === 0 ? 'text-[var(--color-butter)]' : 
                      index % 2 === 0 ? 'text-[var(--color-pancake)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.nickName }}
            </p>
          </div>
          
          <div class="absolute left-[20rem] top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[6.063rem]">
            <p 
              class="text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center"
              :class="index === 0 ? 'text-[var(--color-butter)]' : 
                      index % 2 === 0 ? 'text-[var(--color-pancake)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.score }}p
            </p>
          </div>
          
          <div class="w-[4.5rem] absolute right-4 top-1/2 transform -translate-y-1/2 text-center flex justify-center">
            <TierIcon 
              :tier="item.tier" 
              class="w-[1.8rem] h-[1.8rem]" 
            />
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
        
        <div v-if="isLoading" class="flex-1 flex items-center justify-center bg-[var(--color-cream)] text-[var(--color-choco)] font-['PfStardust30S'] text-xl">
           불러오는 중...
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
          
          <div class="absolute left-45 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[12.625rem]">
            <p 
              class="text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center truncate"
              :class="index === 0 ? 'text-[var(--color-butter)]' : 
                      index % 2 === 0 ? 'text-[var(--color-pancake)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.nickName }}
            </p>
          </div>
          
          <div class="absolute left-[20rem] top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[6.063rem]">
            <p 
              class="text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center"
              :class="index === 0 ? 'text-[var(--color-butter)]' : 
                      index % 2 === 0 ? 'text-[var(--color-pancake)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.score }}p
            </p>
          </div>
          
          <div class="w-[4.5rem] absolute right-4 top-1/2 transform -translate-y-1/2 text-center flex justify-center">
            <TierIcon 
              :tier="item.tier" 
              class="w-[1.8rem] h-[1.8rem]" 
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import TierIcon from '@/shared/ui/TierIcon.vue';
import { computed } from 'vue';
import { useRouter } from 'vue-router';
export interface RankItem {
  id: string;
  nickName: string;
  rank: number;
  score: number;
  tier: string;
}

interface RankingSectionMiniProps {
  privateTop5: RankItem[];
  teamTop5: RankItem[];
  isLoading?: boolean;
}
const props = withDefaults(defineProps<RankingSectionMiniProps>(), {
  privateTop5: () => [], //안에 임시데이터 넣어놓은거 삭제
  teamTop5: () => [],    //안에 임시데이터 넣어놓은거 삭제
  isLoading: false,
});

const router = useRouter();

const goToRanking = () => {
  router.push({ name: 'ranking' });
};

// 상위 5개만 안전하게 슬라이싱
const displayedPrivateRanking = computed(() => {
  return props.privateTop5.slice(0, 5);
});

const displayedTeamRanking = computed(() => {
  return props.teamTop5.slice(0, 5);
});
</script>