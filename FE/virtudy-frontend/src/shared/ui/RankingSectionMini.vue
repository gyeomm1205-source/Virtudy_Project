<template>
  <div class="bg-[var(--color-syrup)] border-2 border-[var(--color-choco)] border-solid h-full w-full overflow-clip" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
    <!-- 개인 랭킹 섹션 -->
    <div class="relative h-[19.625rem] overflow-clip">
      <!-- 제목 -->
      <div class="absolute left-[1.688rem] top-[2.188rem] transform -translate-y-1/2">
        <p class="text-[var(--color-choco)] text-[1.75rem] font-['Xcu'] font-normal leading-normal">
          개인 랭킹
        </p>
      </div>
      
      <!-- 개인 랭킹 리스트 -->
      <div class="absolute left-1/2 top-[calc(50%+1.594rem)] transform -translate-x-1/2 -translate-y-1/2 w-[26.938rem] h-[15.688rem] flex flex-col rounded-[1.25rem] overflow-hidden">
        <div 
          v-for="(item, index) in displayedPersonalRanking" 
          :key="`personal-${index}`"
          class="flex-1 relative w-full"
          :class="index % 2 === 0 ? 'bg-[var(--color-choco)]' : 'bg-[var(--color-cream)]'"
        >
          <!-- 순위 -->
          <div class="absolute left-[1.688rem] top-1/2 transform -translate-y-1/2 w-[1.938rem]">
            <p 
              class="text-[1.75rem] font-['Xcu'] font-normal leading-normal"
              :class="index % 2 === 0 ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.rank }}
            </p>
          </div>
          
          <!-- 닉네임 -->
          <div class="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[12.625rem]">
            <p 
              class="text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center truncate"
              :class="index === 0 ? 'text-[var(--color-butter)]' : 
                     index % 2 === 0 ? 'text-[var(--color-pancake)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.nickname }}
            </p>
          </div>
          
          <!-- 점수 -->
          <div class="absolute left-[19.969rem] top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[6.063rem]">
            <p 
              class="text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center"
              :class="index === 0 ? 'text-[var(--color-butter)]' : 
                     index % 2 === 0 ? 'text-[var(--color-pancake)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.score }}p
            </p>
          </div>
          
          <!-- 왕관 아이콘 -->
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
    
    <!-- 팀 랭킹 섹션 -->
    <div class="relative h-[19.625rem] overflow-clip">
      <!-- 제목 -->
      <div class="absolute left-[1.688rem] top-[1.313rem] transform -translate-y-1/2">
        <p class="text-[var(--color-choco)] text-[1.75rem] font-['Xcu'] font-normal leading-normal">
          팀 랭킹
        </p>
      </div>
      
      <!-- 팀 랭킹 리스트 -->
      <div class="absolute left-1/2 top-[calc(50%+0.719rem)] transform -translate-x-1/2 -translate-y-1/2 w-[26.938rem] h-[15.688rem] flex flex-col rounded-[1.25rem] overflow-hidden">
        <div 
          v-for="(item, index) in displayedTeamRanking" 
          :key="`team-${index}`"
          class="flex-1 relative w-full"
          :class="index % 2 === 0 ? 'bg-[var(--color-choco)]' : 'bg-[var(--color-cream)]'"
        >
          <!-- 순위 -->
          <div class="absolute left-[1.688rem] top-1/2 transform -translate-y-1/2 w-[1.938rem]">
            <p 
              class="text-[1.75rem] font-['Xcu'] font-normal leading-normal"
              :class="index % 2 === 0 ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.rank }}
            </p>
          </div>
          
          <!-- 팀명 -->
          <div class="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[12.625rem]">
            <p 
              class="text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center truncate"
              :class="index === 0 ? 'text-[var(--color-butter)]' : 
                     index % 2 === 0 ? 'text-[var(--color-pancake)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.teamName }}
            </p>
          </div>
          
          <!-- 점수 -->
          <div class="absolute left-[19.969rem] top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[6.063rem]">
            <p 
              class="text-[1.5rem] font-['PfStardust30S'] font-normal leading-none text-center"
              :class="index === 0 ? 'text-[var(--color-butter)]' : 
                     index % 2 === 0 ? 'text-[var(--color-pancake)]' : 'text-[var(--color-pancake)]'"
            >
              {{ item.score }}p
            </p>
          </div>
          
          <!-- 왕관 아이콘 -->
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

interface RankingItem {
  rank: number;
  nickname?: string;
  teamName?: string;
  score: string;
}

interface RankingSectionMiniProps {
  personalRanking?: RankingItem[];
  teamRanking?: RankingItem[];
}

// Props 정의
const props = withDefaults(defineProps<RankingSectionMiniProps>(), {
  personalRanking: () => [
    { rank: 1, nickname: "피그마는너무어려워", score: "2450" },
    { rank: 2, nickname: "그래도할만해요", score: "2398" },
    { rank: 3, nickname: "3팀화이팅", score: "2267" },
    { rank: 4, nickname: "아니진짜어렵다니깐", score: "2156" },
    { rank: 5, nickname: "오늘처음써봐요", score: "2089" },
  ],
  teamRanking: () => [
    { rank: 1, teamName: "3팀알고리즘", score: "8950" },
    { rank: 2, teamName: "3팀자료구조", score: "8756" },
    { rank: 3, teamName: "3팀컴퓨터사이언스", score: "8623" },
    { rank: 4, teamName: "5J1P", score: "8489" },
    { rank: 5, teamName: "버터디스터디", score: "8356" },
  ],
});

// 상위 5개만 표시
const displayedPersonalRanking = computed(() => {
  return props.personalRanking.slice(0, 5);
});

const displayedTeamRanking = computed(() => {
  return props.teamRanking.slice(0, 5);
});
</script>