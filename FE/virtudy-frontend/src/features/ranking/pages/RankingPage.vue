<template>
  <div class="min-h-screen bg-[var(--color-syrup)] relative w-full overflow-hidden">
    <GlobalNavBar />
    
    <div class="relative pt-[75px] pb-[200px] min-h-[calc(100vh-128px)]">
      
      <div class="absolute left-[77px] top-[39px] w-[54px] h-[54px] cursor-pointer hover:scale-110 transition-transform z-10" @click="goBack">
        <svg viewBox="0 0 54 54" class="w-full h-full" fill="var(--color-choco)">
          <path d="M40 22H18.8L29.4 11.4L27 9L13 23L27 37L29.4 34.6L18.8 24H40V22Z"/>
        </svg>
      </div>
      
      <div class="absolute left-[59px] top-[244px] transform -translate-y-1/2">
        <h1 class="text-[var(--color-pancake)] text-[156px] font-['Ram'] font-medium leading-none tracking-[-18.72px]">
          랭킹
        </h1>
      </div>
      <div class="absolute left-[calc(25%+31px)] top-[179px] w-[664px] h-[188px]">
        <div class="absolute left-[23px] top-[-7px] w-[146px] h-[146px] rounded-full overflow-hidden border-4 border-[var(--color-choco)]">
          <img 
            src="http://localhost:3845/assets/ae7ca0939b29738c16aee5cf86953e893d60c594.svg"
            alt="프로필 사진"
            class="w-full h-full object-cover"
          />
        </div>
        <div class="absolute left-[162px] top-[30px]">
          <p class="text-[var(--color-choco)] text-[42px] font-['Ram'] font-medium leading-[48px] tracking-[-0.84px]">
            {{ myRankInfo?.nickName || authStore.userInfo?.nickName || '정보없음' }}
          </p>
        </div>
        
        <div class="absolute left-[162px] top-[80px]">
          <p class="text-[var(--color-choco)] text-[32px] font-['Xcu'] font-normal leading-normal">
            {{ rankType === 'private' ? '님의 순위는...' : '팀의 순위는...' }}
          </p>
        </div>
        
        <div class="absolute left-[371px] top-[50px]">
          <p class="text-[var(--color-butter)] text-[130px] font-['Xcu'] font-normal leading-normal drop-shadow-md">
            {{ myRankInfo?.rank ? myRankInfo.rank + '위' : '-' }}
          </p>
        </div>
      </div>
      
      <div class="absolute left-[calc(8.33%+109px)] top-[139px] w-[982px]">
        <div class="w-[982px] h-[658px]">
          
          <div class="h-[62px] relative mb-[2px]">
            <div class="absolute left-[31px] top-[9px] flex gap-0">
              <button 
                @click="changeType('private')"
                :class="[
                  'border-2 border-[var(--color-choco)] border-solid px-[32px] py-[20px] rounded-tl-[30px] rounded-tr-[30px] rounded-bl-[2px] rounded-br-[2px]',
                  rankType === 'private' ? 'bg-[var(--color-butter)]' : 'bg-[var(--color-cream)]'
                ]"
                style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
              >
                <span class="text-[var(--color-choco)] text-[24px] font-['PfStardust30S'] font-normal leading-none">
                  개인
                </span>
              </button>
              
              <button 
                @click="changeType('team')"
                :class="[
                  'border-2 border-[var(--color-choco)] border-solid px-[32px] py-[20px] rounded-tl-[30px] rounded-tr-[30px] rounded-bl-[2px] rounded-br-[2px]',
                  rankType === 'team' ? 'bg-[var(--color-butter)]' : 'bg-[var(--color-cream)]'
                ]"
                style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
              >
                <span class="text-[var(--color-choco)] text-[24px] font-['PfStardust30S'] font-normal leading-none">
                  팀
                </span>
              </button>
            </div>
            
            <div class="absolute right-0 top-[14px] w-[393px] border-2 border-[var(--color-choco)] border-solid bg-[var(--color-cream2)] h-[33px] flex items-center px-[7px] gap-[10px]" style="box-shadow: 3.667px 3.667px 0px 0px var(--color-choco);">
              <div class="w-[21px] h-[21px] flex items-center justify-center">
                <svg viewBox="0 0 21 21" class="w-[19.636px] h-[19.636px]">
                  <path d="M8 2C11.314 2 14 4.686 14 8C14 9.248 13.587 10.397 12.897 11.324L18.707 17.071C19.098 17.461 19.098 18.095 18.707 18.485C18.317 18.876 17.683 18.876 17.293 18.485L11.486 12.678C10.559 13.368 9.41 13.781 8.162 13.781C4.848 13.781 2.162 11.095 2.162 7.781C2.162 4.467 4.848 1.781 8.162 1.781Z" fill="var(--color-choco)"/>
                </svg>
              </div>
              <input 
                v-model="searchKeyword"
                @keyup.enter="handleSearch"
                type="text" 
                placeholder="Search"
                class="flex-1 bg-transparent border-none outline-none text-[var(--color-syrup)] text-[18px] font-['PfStardust30S'] font-normal leading-normal tracking-[-0.7px]"
              />
            </div>
          </div>
          
          <div class="border-2 border-[var(--color-choco)] border-solid h-[600px] w-[981px] rounded-[20px] overflow-clip flex flex-col items-center justify-center bg-[var(--color-cream)]" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
            <div class="w-full h-full flex flex-col">
              
              <div v-if="isLoading" class="w-full h-full flex items-center justify-center text-[24px] font-['PfStardust30S'] text-[var(--color-choco)]">
                Loading...
              </div>

              <div v-else-if="rankList.length === 0" class="w-full h-full flex items-center justify-center text-[24px] font-['PfStardust30S'] text-[var(--color-choco)]">
                랭킹 데이터가 없습니다.
              </div>

              <div 
                v-else
                v-for="(item, index) in rankList" 
                :key="item.email || index"
                class="flex-1 relative w-full border-b border-[var(--color-syrup)] last:border-none"
                :class="[
                  index % 2 === 0 ? 'bg-[var(--color-choco)]' : 'bg-[var(--color-cream)]',
                  { '!bg-[var(--color-choco)] !border-2 !border-[var(--color-butter)] text-[var(--color-butter)]': isMyself(item) }
                ]"
              >
                <div class="absolute left-[27px] top-1/2 transform -translate-y-1/2 w-[50px] text-center">
                  <p 
                    class="text-[28px] font-['Xcu'] font-normal leading-normal"
                    :class="[
                      isMyself(item) ? 'text-[var(--color-butter)]' : 
                      index % 2 === 0 ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'
                    ]"
                  >
                    {{ item.rank }}
                  </p>
                </div>
                
                <div class="absolute left-[491px] top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[300px] text-center">
                  <p 
                    class="text-[24px] font-['PfStardust30S'] font-normal leading-none truncate w-full block"
                    :class="[
                      isMyself(item) ? 'text-[var(--color-butter)]' : 
                      (index % 2 === 0 && index < 3) || (index % 2 === 1 && index === 1) || (index % 2 === 0 && index === 6) || (index % 2 === 0 && index === 8) ? 'text-[var(--color-butter)]' : 'text-[var(--color-pancake)]'
                    ]"
                  >
                     {{ item.nickName }}
                     <span v-if="isMyself(item)" class="text-[14px] ml-2">(나)</span>
                  </p>
                </div>
                
                <div class="absolute left-[873.5px] top-1/2 transform -translate-x-1/2 -translate-y-1/2">
                  <p 
                    class="text-[24px] font-['PfStardust30S'] font-normal leading-none text-center w-[97px]"
                    :class="[
                      isMyself(item) ? 'text-[var(--color-butter)]' : 
                      (index % 2 === 0 && index < 3) || (index % 2 === 1 && index === 1) || (index % 2 === 0 && index === 6) || (index % 2 === 0 && index === 8) ? 'text-[var(--color-butter)]' : 'text-[var(--color-pancake)]'
                    ]"
                  >
                    {{ item.score }}p
                  </p>
                </div>
                
                <div class="absolute right-[25px] top-1/2 transform -translate-y-1/2 w-[24px] h-[24px]">
                  <svg viewBox="0 0 24 24" class="w-full h-full">
                    <path 
                      d="M5 16L3 8L9 10L12 6L15 10L21 8L19 16H5Z" 
                      :fill="(index % 2 === 0 && index < 3) || (index % 2 === 1 && index === 1) || (index % 2 === 0 && index === 6) || (index % 2 === 0 && index === 8) ? 'var(--color-butter)' : 'var(--color-syrup)'"
                      stroke="var(--color-choco)" 
                      stroke-width="1"
                    />
                  </svg>
                </div>
              </div>
            </div>
          </div>
          
          <div class="h-[28px] mt-[20px] flex items-center justify-center relative">
            <div class="flex items-center gap-[24px]">
              
              <button 
                @click="changePage(0)" 
                :disabled="currentPage === 0"
                class="w-[28px] h-[28px] relative disabled:opacity-50 hover:scale-110 transition-transform"
              >
                <svg viewBox="0 0 28 28" class="w-full h-full">
                  <rect width="28" height="28" fill="var(--color-choco)" opacity="0.3"/>
                </svg>
                <svg viewBox="0 0 20 20" class="absolute inset-1">
                  <path d="M15 5L10 10L15 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
                  <path d="M11 5L6 10L11 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
                </svg>
              </button>
              
              <button 
                @click="changePage(Math.max(0, currentPage - 1))" 
                :disabled="currentPage === 0"
                class="w-[28px] h-[28px] relative disabled:opacity-50 hover:scale-110 transition-transform"
              >
                <svg viewBox="0 0 28 28" class="w-full h-full">
                  <rect width="28" height="28" fill="var(--color-choco)" opacity="0.3"/>
                </svg>
                <svg viewBox="0 0 20 20" class="absolute inset-1">
                  <path d="M12 5L7 10L12 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
                </svg>
              </button>
              
              <div class="flex items-center gap-[12px]">
                <button 
                  v-for="page in visiblePages" 
                  :key="page"
                  @click="changePage(page)"
                  class="w-[28px] h-[28px] flex items-center justify-center hover:scale-110 transition-transform"
                >
                  <span 
                    :class="[
                      'text-[20px] font-[PfStardust30S] font-normal leading-none tracking-[-0.8px] text-center',
                      page === currentPage ? 'text-[var(--color-butter)]' : 'text-[var(--color-choco)]'
                    ]"
                    :style="page === currentPage ? 'text-shadow: 1px 1px 0px var(--color-butter2);' : ''"
                  >
                    {{ page + 1 }}
                  </span>
                </button>
              </div>
              
              <button 
                @click="changePage(Math.min(totalPages - 1, currentPage + 1))" 
                :disabled="currentPage >= totalPages - 1"
                class="w-[28px] h-[28px] relative disabled:opacity-50 hover:scale-110 transition-transform"
              >
                <svg viewBox="0 0 28 28" class="w-full h-full">
                  <rect width="28" height="28" fill="var(--color-choco)" opacity="0.3"/>
                </svg>
                <svg viewBox="0 0 20 20" class="absolute inset-1">
                  <path d="M8 5L13 10L8 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
                </svg>
              </button>
              
              <button 
                @click="changePage(totalPages - 1)" 
                :disabled="currentPage >= totalPages - 1"
                class="w-[28px] h-[28px] relative disabled:opacity-50 hover:scale-110 transition-transform"
              >
                <svg viewBox="0 0 28 28" class="w-full h-full">
                  <rect width="28" height="28" fill="var(--color-choco)" opacity="0.3"/>
                </svg>
                <svg viewBox="0 0 20 20" class="absolute inset-1">
                  <path d="M5 5L10 10L5 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
                  <path d="M9 5L14 10L9 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <GlobalFooter />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../../stores/authStore';
import { useRanking } from '../logic/useRanking';
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';

// [중요] 타입 임포트
import type { RankItem } from '../types/ranking.types';

const authStore = useAuthStore();
const router = useRouter();

// [중요] useRanking 훅에서 로직과 상태를 모두 가져옴
const { 
  rankType, searchKeyword, rankList, myRankInfo, isLoading,
  currentPage, visiblePages, totalPages,
  changeType, changePage, handleSearch 
} = useRanking();

const goBack = () => {
  if (authStore.isLoggedIn) {
    router.push({ name: 'user' });
  } else {
    router.push({ name: 'guest' });
  }
};

// [중요] 본인 확인 로직
const isMyself = (item: RankItem) => {
  if (!authStore.userInfo?.email) return false;
  return item.email === authStore.userInfo.email;
};
</script>

<style scoped>
/* 추가적인 스타일이 필요하다면 여기에 작성 */
</style>