<template>
  <div class="bg-[var(--color-syrup)] border-2 border-[var(--color-choco)] border-solid h-[635px] w-[473px] relative overflow-clip">
    <!-- 개인 랭킹 섹션 -->
    <div class="h-[314px] w-full relative">
      <!-- 개인 랭킹 제목 -->
      <div class="absolute left-[27px] top-[35px] transform -translate-y-1/2">
        <p class="text-[var(--color-choco)] text-[28px] font-['Xcu'] font-medium leading-none">
          개인 랭킹
        </p>
      </div>
      
      <!-- 개인 랭킹 리스트 -->
      <div class="absolute left-1/2 top-[calc(50%+25.5px)] transform -translate-x-1/2 -translate-y-1/2 w-[431px] h-[251px] overflow-clip rounded-[20px] flex flex-col">
        <div 
          v-for="(item, index) in personalRanking" 
          :key="`personal-${index}`"
          class="flex-1 relative w-full"
          :class="[
            index % 2 === 0 ? 'bg-[var(--color-choco)]' : 'bg-[var(--color-cream)]'
          ]"
        >
          <!-- 순위 -->
          <div class="absolute left-[27px] top-1/2 transform -translate-y-1/2">
            <p 
              class="text-[28px] font-['Xcu'] font-normal leading-normal"
              :class="[
                index % 2 === 0 ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'
              ]"
            >
              {{ index + 1 }}
            </p>
          </div>
          
          <!-- 닉네임 -->
          <div class="absolute left-[170px] top-1/2 transform -translate-x-1/2 -translate-y-1/2">
            <p 
              class="text-[24px] font-['PfStardust30S'] font-normal leading-none text-center w-[202px] truncate"
              :class="[
                index % 2 === 0 ? 'text-[var(--color-butter)]' : 'text-[var(--color-pancake)]'
              ]"
            >
              {{ item.nickname }}
            </p>
          </div>
          
          <!-- 점수 -->
          <div class="absolute right-[97px] top-1/2 transform -translate-y-1/2">
            <p 
              class="text-[24px] font-['PfStardust30S'] font-normal leading-none text-center w-[97px]"
              :class="[
                index % 2 === 0 ? 'text-[var(--color-butter)]' : 'text-[var(--color-pancake)]'
              ]"
            >
              {{ item.score }}p
            </p>
          </div>
          
          <!-- 왕관 아이콘 -->
          <div class="absolute right-[25px] top-1/2 transform -translate-y-1/2 w-[24px] h-[24px]">
            <img 
              :src="index === 0 ? goldCrownUrl : silverCrownUrl" 
              alt="왕관"
              class="w-full h-full object-contain"
            />
          </div>
        </div>
      </div>
    </div>
    
    <!-- 팀 랭킹 섹션 -->
    <div class="absolute top-[319px] left-0 h-[314px] w-full">
      <!-- 팀 랭킹 제목 -->
      <div class="absolute left-[27px] top-[21px] transform -translate-y-1/2">
        <p class="text-[var(--color-choco)] text-[28px] font-['Xcu'] font-medium leading-none">
          팀 랭킹
        </p>
      </div>
      
      <!-- 팀 랭킹 리스트 -->
      <div class="absolute left-1/2 top-[calc(50%+11.5px)] transform -translate-x-1/2 -translate-y-1/2 w-[431px] h-[251px] overflow-clip rounded-[20px] flex flex-col">
        <div 
          v-for="(item, index) in teamRanking" 
          :key="`team-${index}`"
          class="flex-1 relative w-full"
          :class="[
            index % 2 === 0 ? 'bg-[var(--color-choco)]' : 'bg-[var(--color-cream)]'
          ]"
        >
          <!-- 순위 -->
          <div class="absolute left-[27px] top-1/2 transform -translate-y-1/2">
            <p 
              class="text-[28px] font-['Xcu'] font-normal leading-normal"
              :class="[
                index % 2 === 0 ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'
              ]"
            >
              {{ index + 1 }}
            </p>
          </div>
          
          <!-- 팀명 -->
          <div class="absolute left-[170px] top-1/2 transform -translate-x-1/2 -translate-y-1/2">
            <p 
              class="text-[24px] font-['PfStardust30S'] font-normal leading-none text-center w-[202px] truncate"
              :class="[
                index % 2 === 0 ? 'text-[var(--color-butter)]' : 'text-[var(--color-pancake)]'
              ]"
            >
              {{ item.teamName }}
            </p>
          </div>
          
          <!-- 점수 -->
          <div class="absolute right-[97px] top-1/2 transform -translate-y-1/2">
            <p 
              class="text-[24px] font-['PfStardust30S'] font-normal leading-none text-center w-[97px]"
              :class="[
                index % 2 === 0 ? 'text-[var(--color-butter)]' : 'text-[var(--color-pancake)]'
              ]"
            >
              {{ item.score }}p
            </p>
          </div>
          
          <!-- 왕관 아이콘 -->
          <div class="absolute right-[25px] top-1/2 transform -translate-y-1/2 w-[24px] h-[24px]">
            <img 
              :src="index === 0 ? goldCrownUrl : silverCrownUrl" 
              alt="왕관"
              class="w-full h-full object-contain"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface RankingItem {
  nickname?: string;
  teamName?: string;
  score: string;
}

interface RankingSectionProps {
  personalRanking?: RankingItem[];
  teamRanking?: RankingItem[];
}

// Props 정의
const props = withDefaults(defineProps<RankingSectionProps>(), {
  personalRanking: () => [
    { nickname: "피그마는너무어려워", score: "000" },
    { nickname: "그래도할만해요", score: "000" },
    { nickname: "3팀화이팅", score: "000" },
    { nickname: "아니진짜어렵다니깐", score: "000" },
    { nickname: "오늘처음써봐요", score: "000" },
  ],
  teamRanking: () => [
    { teamName: "3팀알고리즘", score: "000" },
    { teamName: "3팀자료구조", score: "000" },
    { teamName: "3팀컴퓨터사이언스", score: "000" },
    { teamName: "5J1P", score: "000" },
    { teamName: "버터디스터디", score: "000" },
  ],
});

// 이미지 URLs (Figma에서 제공된 이미지들)
const goldCrownUrl = "http://localhost:3845/assets/4a9510a86640eb060a63cd683de14a133bd542bf.svg";
const silverCrownUrl = "http://localhost:3845/assets/ebe1231bbda7306f7f3d5749ef9c081e52384069.svg";
</script>