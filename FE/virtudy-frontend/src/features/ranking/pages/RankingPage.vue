<template>
  <div class="min-h-screen bg-[var(--color-syrup)] relative w-full flex flex-col">
    <GlobalNavBar />
    
    <div class="flex-1 flex flex-col items-center pt-[5rem] pb-[8rem] w-full min-h-[calc(100vh-200px)]">
      
      <button 
      @click="goBack"
      class="absolute left-[4.75rem] top-[7.4375rem] w-[4rem] h-[4rem] cursor-pointer hover:scale-110 transition-transform"
      >
        <svg 
          xmlns="http://www.w3.org/2000/svg" 
          fill="none" 
          viewBox="0 0 24 24" 
          stroke-width="1.5" 
          stroke="var(--color-choco)" 
          class="w-full h-full"
        >
          <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
        </svg>
      </button>

      <div class="w-[61.375rem] flex items-end justify-between mb-[1rem] relative">
        
        <h1 class="text-[var(--color-pancake)] text-[9.75rem] font-['Ram'] font-medium leading-none tracking-[-1.17rem] transform translate-y-[1rem]">
          랭킹
        </h1>

        <div class="flex items-center gap-[1.5rem] mb-[1rem]">
          <div class="w-[9.125rem] h-[9.125rem] rounded-full overflow-hidden border-4 border-[var(--color-choco)] bg-[var(--color-cream)] shadow-md">
            <img 
              v-if="authStore.userInfo?.avatarImageUrl"
              :src="authStore.userInfo.avatarImageUrl"
              alt="프로필"
              class="w-full h-full object-cover"
            />
            <div v-else class="w-full h-full flex items-center justify-center text-[var(--color-choco)] font-bold text-xl">
              ME
            </div>
          </div>

          <div class="flex flex-col items-start">
             <div class="flex items-end gap-[0.5rem]">
                <span class="text-[var(--color-choco)] text-[2.625rem] font-['Ram'] font-medium leading-none">
                  {{ myRankInfo?.nickName || authStore.userInfo?.nickName || '방문자' }}
                </span>
                <span class="text-[var(--color-choco)] text-[2rem] font-['Xcu'] font-normal pb-[0.2rem]">
                  {{ rankType === 'private' ? '님의 순위는...' : '팀의 순위는...' }}
                </span>
             </div>
             <div class="text-[var(--color-butter)] text-[8.125rem] font-['Xcu'] font-normal leading-none drop-shadow-md mt-[-0.5rem]">
                {{ myRankInfo?.rank ? myRankInfo.rank + '위' : '-' }}
             </div>
          </div>
        </div>
      </div>
      
      <div class="w-[61.375rem] relative">
        
        <div class="flex justify-between items-end mb-[-2px] relative z-10 px-[2rem]">
          <div class="flex gap-0">
            <button 
              @click="changeType('private')"
              :class="[
                'border-2 border-[var(--color-choco)] border-b-0 px-[2rem] py-[1.25rem] rounded-t-[1.875rem]',
                rankType === 'private' ? 'bg-[var(--color-butter)] h-[4.5rem]' : 'bg-[var(--color-cream)] h-[3.8rem] mt-[0.7rem]'
              ]"
            >
              <span class="text-[var(--color-choco)] text-[1.5rem] font-['PfStardust30S']">개인</span>
            </button>
            <button 
              @click="changeType('team')"
              :class="[
                'border-2 border-[var(--color-choco)] border-b-0 px-[2rem] py-[1.25rem] rounded-t-[1.875rem] ml-[-2px]',
                rankType === 'team' ? 'bg-[var(--color-butter)] h-[4.5rem]' : 'bg-[var(--color-cream)] h-[3.8rem] mt-[0.7rem]'
              ]"
            >
              <span class="text-[var(--color-choco)] text-[1.5rem] font-['PfStardust30S']">팀</span>
            </button>
          </div>

          <div class="w-[24.5rem] h-[2.5rem] border-2 border-[var(--color-choco)] bg-[var(--color-cream2)] flex items-center px-[0.5rem] gap-[0.5rem] mb-[0.5rem] shadow-[4px_4px_0px_0px_var(--color-choco)]">
            <svg viewBox="0 0 21 21" class="w-[1.25rem] h-[1.25rem]">
               <path d="M8 2C11.314 2 14 4.686 14 8C14 9.248 13.587 10.397 12.897 11.324L18.707 17.071C19.098 17.461 19.098 18.095 18.707 18.485C18.317 18.876 17.683 18.876 17.293 18.485L11.486 12.678C10.559 13.368 9.41 13.781 8.162 13.781C4.848 13.781 2.162 11.095 2.162 7.781C2.162 4.467 4.848 1.781 8.162 1.781Z" fill="var(--color-choco)"/>
            </svg>
            <input 
              v-model="searchKeyword"
              @keyup.enter="handleSearch"
              type="text" 
              placeholder="Search"
              class="flex-1 bg-transparent border-none outline-none text-[var(--color-syrup)] text-[1.125rem] font-['PfStardust30S'] placeholder-[var(--color-syrup)] opacity-70"
            />
          </div>
        </div>
        
        <div class="border-2 border-[var(--color-choco)] w-full h-[37.5rem] rounded-[1.25rem] overflow-hidden bg-[var(--color-cream)] flex flex-col relative z-0 shadow-[4px_4px_0px_0px_var(--color-choco)]">
          
          <div v-if="isLoading" class="flex-1 flex items-center justify-center text-[1.5rem] font-['PfStardust30S'] text-[var(--color-choco)]">
            Loading...
          </div>
          <div v-else-if="rankList.length === 0" class="flex-1 flex items-center justify-center text-[1.5rem] font-['PfStardust30S'] text-[var(--color-choco)]">
            랭킹 데이터가 없습니다.
          </div>

          <div 
            v-else
            v-for="(item, index) in rankList" 
            :key="item.id"
            class="flex items-center h-[3.75rem] border-b border-[var(--color-syrup)] last:border-none px-[2rem]"
            :class="[
              index % 2 === 0 ? 'bg-[var(--color-choco)]' : 'bg-[var(--color-cream)]',
              { '!bg-[var(--color-choco)] !border-2 !border-[var(--color-butter)] box-border': isMyself(item) }
            ]"
          >
            <div class="w-[4rem] text-center">
              <span 
                class="text-[1.75rem] font-['Xcu']"
                :class="[
                   isMyself(item) ? 'text-[var(--color-butter)]' : 
                   (index % 2 === 0) ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'
                ]"
              >
                {{ item.rank }}
              </span>
            </div>

            <div class="flex-1 text-center px-[1rem]">
              <span 
                class="text-[1.5rem] font-['PfStardust30S'] truncate block"
                :class="[
                  isMyself(item) ? 'text-[var(--color-butter)]' :
                  (index % 2 === 0 && index < 3) ? 'text-[var(--color-butter)]' : 
                  (index === 1) ? 'text-[var(--color-butter)]' : 
                  'text-[var(--color-pancake)]'
                ]"
              >
                {{ item.nickName }}
                <span v-if="isMyself(item)" class="text-[0.8rem] ml-1 align-top">(나)</span>
              </span>
            </div>

            <div class="w-[6rem] text-right">
              <span 
                class="text-[1.5rem] font-['PfStardust30S']"
                :class="[
                   isMyself(item) ? 'text-[var(--color-butter)]' : 
                   (index % 2 === 0) ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'
                ]"
              >
                {{ item.score }}p
              </span>
            </div>

            <div class="w-[2rem] ml-[1rem] flex justify-center">
              <svg viewBox="0 0 24 24" class="w-[1.5rem] h-[1.5rem]">
                <path 
                  d="M5 16L3 8L9 10L12 6L15 10L21 8L19 16H5Z" 
                  :fill="(index < 3) ? 'var(--color-butter)' : 'var(--color-syrup)'"
                  stroke="var(--color-choco)" 
                  stroke-width="1"
                />
              </svg>
            </div>
          </div>
        </div>
        
        <div class="mt-[1.5rem] flex justify-center gap-[1rem]">
          <button @click="changePage(0)" :disabled="currentPage === 0" class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform">
             <svg viewBox="0 0 20 20" class="w-full h-full stroke-[var(--color-choco)] stroke-2 fill-none"><path d="M15 5L10 10L15 15"/><path d="M11 5L6 10L11 15"/></svg>
          </button>
          <button @click="changePage(Math.max(0, currentPage - 1))" :disabled="currentPage === 0" class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform">
             <svg viewBox="0 0 20 20" class="w-full h-full stroke-[var(--color-choco)] stroke-2 fill-none"><path d="M13 5L8 10L13 15"/></svg>
          </button>

          <div class="flex gap-[0.75rem]">
            <button 
              v-for="page in visiblePages" 
              :key="page"
              @click="changePage(page)"
              class="w-[2rem] h-[2rem] flex items-center justify-center hover:scale-110 transition-transform font-['PfStardust30S'] text-[1.25rem]"
              :class="page === currentPage ? 'text-[var(--color-butter)] drop-shadow-[1px_1px_0_var(--color-choco)]' : 'text-[var(--color-choco)]'"
            >
              {{ page + 1 }}
            </button>
          </div>

          <button @click="changePage(Math.min(totalPages - 1, currentPage + 1))" :disabled="currentPage >= totalPages - 1" class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform">
             <svg viewBox="0 0 20 20" class="w-full h-full stroke-[var(--color-choco)] stroke-2 fill-none"><path d="M7 5L12 10L7 15"/></svg>
          </button>
          <button @click="changePage(totalPages - 1)" :disabled="currentPage >= totalPages - 1" class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform">
             <svg viewBox="0 0 20 20" class="w-full h-full stroke-[var(--color-choco)] stroke-2 fill-none"><path d="M5 5L10 10L5 15"/><path d="M9 5L14 10L9 15"/></svg>
          </button>
        </div>

      </div>

    </div>
    
    <GlobalFooter />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore'; 
import { useRanking } from '../logic/useRanking';
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';

// [중요] RankItem 인터페이스 임포트 (Ranking.types.ts에 정의된 것)
import type { RankItem } from '../types/ranking.types';

const authStore = useAuthStore();
const router = useRouter();

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

// [타입 대응] 본인 확인 로직
// item.email과 authStore의 이메일을 비교
const isMyself = (item: RankItem) => {
  if (!authStore.userInfo?.email) return false;
  return item.email === authStore.userInfo.email;
};
</script>