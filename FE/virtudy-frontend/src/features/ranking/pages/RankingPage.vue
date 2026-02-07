<template>
  <GlobalBackground :skyType="3">
  <div class="min-h-screen relative w-full flex flex-col">

    <div
      class="absolute left-[30rem] top-[20.3rem] z-0 rank-avatar"
      :style="{ width: '14rem', height: '16rem' }"
    >
      <CharacterAvatar
        v-if="hasAvatarConfig"
        :config="displayAvatar!"
        class="w-full h-full"
      />
    </div>

    <div class="flex-1 flex flex-col pt-[4.5rem] pb-[6.5rem] w-full min-h-[calc(100vh-200px)] px-[4.75rem] rank-content">
      <div class="w-full max-w-[74rem] mx-auto relative rank-shell">
        <button @click="goBack"
          class="absolute -left-[2.78rem] top-[2.95rem] w-[4rem] h-[4rem] cursor-pointer hover:scale-110 transition-transform rank-back">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
            stroke="var(--color-choco)" class="w-full h-full">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
          </svg>
        </button>

        <div class="flex items-end justify-start gap-[3.5rem] mt-[4.5rem] mb-[1rem]">
          <!-- 랭킹 제목 -->
          <h1 class="text-[var(--color-pancake)] [text-shadow:4px_4px_0px_var(--color-choco)] text-[clamp(4rem,9.75rem,9.75rem)] font-['Ram'] font-medium leading-none tracking-[-1.17rem] mt-[6.5rem] -ml-[4rem] whitespace-nowrap">
            랭킹
          </h1>

          <!-- 아바타와 순위 정보 -->
          <div class="flex items-center translate-x-[20rem]">
            <div class="flex flex-col items-start">
              <div class="flex items-end gap-[0.5rem] -mt-[5rem]">
                <span
                  class="text-[var(--color-choco)] text-[clamp(1.5rem,2.625rem,2.625rem)] font-['Ram'] font-medium leading-tight max-w-[22rem] whitespace-normal break-words"
                  style="display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;"
                >
                  {{ myRankInfo?.nickName || authStore.userInfo?.nickName || '방문자' }}
                </span>

                <span class="text-[var(--color-choco)] text-[clamp(1rem,2rem,2rem)] font-['Xcu'] font-normal pb-[0.2rem]">
                  <template v-if="rankType === 'team' && !myRankInfo">
                    님의 스터디는...
                  </template>
                  <template v-else>
                    {{ rankType === 'private' ? '님의 순위는...' : '팀의 스터디 순위는...' }}
                  </template>
                </span>
              </div>

              <div class="text-[var(--color-butter)] [-webkit-text-stroke:3px_var(--color-syrup)] font-['Xcu'] font-normal drop-shadow-md mt-[-0.5rem] translate-x-[14rem]">
                <template v-if="myRankInfo?.rank">
                  <span class="leading-none  text-[clamp(3rem,8.125rem,8.125rem)]">
                    {{ myRankInfo.rank }}위
                  </span>
                </template>

                <template v-else>
                  <span class="tracking-tighter leading-none text-[clamp(2rem,3.5rem,3.5rem)] max-w-[18rem] truncate">
                    {{ rankType === 'private' ? '환영합니다!' : '최애 스터디가 없습니다!' }}
                  </span>
                </template>
              </div>
            </div>
          </div>
        </div>

      <!-- 탭과 검색바 -->
        <div class="max-w-[61.375rem] w-full ml-26 mr-0 relative mb-[2rem] translate-x-[0.5rem] rank-panel">

        <div class="flex justify-between items-end mb-[-2px] relative z-0 px-[2rem] rank-panel-header">
          <div class="flex items-center gap-[1rem] rank-tabs">
            <div class="flex gap-0 ">
              <button @click="changeType('private')" :class="[
                'filter-tab border-2 border-[var(--color-choco)] border-solid px-[32px] py-[10px] rounded-tl-[30px] rounded-tr-[30px] rounded-bl-[2px] rounded-br-[2px] min-w-[96px]',
                rankType === 'private' ? 'bg-[var(--color-butter)] tab-active' : 'bg-[var(--color-cream)] tab-inactive'
              ]">
                <span class="text-[var(--color-choco)] text-[1.5rem] font-['PfStardust30S']">개인</span>
              </button>
              <button @click="changeType('team')" :class="[
                'filter-tab border-2 border-[var(--color-choco)] border-solid px-[32px] py-[10px] rounded-tl-[30px] rounded-tr-[30px] rounded-bl-[2px] rounded-br-[2px] ml-[-2px] min-w-[96px]',
                rankType === 'team' ? 'bg-[var(--color-butter)] tab-active' : 'bg-[var(--color-cream)] tab-inactive'
              ]">
                <span class="text-[var(--color-choco)] text-[1.5rem] font-['PfStardust30S']">팀</span>
              </button>
            </div>
          </div>

          <div
            class="w-[min(24.5625rem,40vw)] h-[2.0625rem] border-2 border-[var(--color-choco)] bg-[var(--color-cream2)] flex items-center px-[0.5rem] gap-[0.5rem] mb-[0.5rem] shadow-[4px_4px_0px_0px_var(--color-choco)] translate-y-[-0.9rem] translate-x-[1rem] rank-search">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" fill="none" class="w-[1.25rem] h-[1.25rem]">
              <path d="M17.1819 15.5455V14.7273H16.3637V13.9091H15.5455V13.0909H14.7273V12.2728H13.0909V11.4546H13.9091V9.8182H14.7273V4.9091H13.9091V3.27273H13.0909V2.45455H12.2728V1.63637H11.4546V0.818183H9.8182V0H4.9091V0.818183H3.27273V1.63637H2.45455V2.45455H1.63637V3.27273H0.818183V4.9091H0V9.8182H0.818183V11.4546H1.63637V12.2728H2.45455V13.0909H3.27273V13.9091H4.9091V14.7273H9.8182V13.9091H11.4546V13.0909H12.2728V14.7273H13.0909V15.5455H13.9091V16.3637H14.7273V17.1819H15.5455V18H17.1819V17.1819H18V15.5455H17.1819ZM9.00002 11.4546V12.2728H5.72728V11.4546H4.09092V10.6364H3.27273V9.00002H2.45455V5.72728H3.27273V4.09092H4.09092V3.27273H5.72728V2.45455H9.00002V3.27273H10.6364V4.09092H11.4546V5.72728H12.2728V9.00002H11.4546V10.6364H10.6364V11.4546H9.00002Z" fill="#805143"/>
            </svg>
            <input 
            :value="searchKeyword" 
            @input="(e) => {
              searchKeyword = (e.target as HTMLInputElement).value;
              handleSearch(); // 혹은 필요한 검색 로직 함수 호출
            }"
            @paste="handleSearch" 
            @keyup.enter="handleSearch" 
            type="text" 
            placeholder="Search"
            class="flex-1 bg-transparent border-none outline-none text-[var(--color-syrup)] text-[1.125rem] font-['PfStardust30S'] placeholder-[var(--color-syrup)] opacity-70" 
          />
          </div>
        </div>

        <div
          class="mt-[-3.8px] border-2 border-[var(--color-choco)] w-full h-[37.5rem] rounded-[1.25rem] overflow-hidden bg-[var(--color-cream)] flex flex-col relative z-10 shadow-[4px_4px_0px_0px_var(--color-choco)] rank-table">

          <div v-if="isLoading"
            class="flex-1 flex items-center justify-center text-[1.5rem] font-['PfStardust30S'] text-[var(--color-choco)]">
            Loading...
          </div>
          <div v-else-if="rankList.length === 0"
            class="flex-1 flex items-center justify-center text-[1.5rem] font-['PfStardust30S'] text-[var(--color-choco)]">
            랭킹 데이터가 없습니다.
          </div>

          <div v-else v-for="(item, index) in rankList" :key="item.id"
            class="flex items-center h-[3.75rem] border-b border-[var(--color-syrup)] last:border-none px-[2rem] rank-row"
            :class="[
              /* 배경색 (짝수: 초코, 홀수: 크림) */
              index % 2 === 0 ? 'bg-[var(--color-choco)]' : 'bg-[var(--color-cream)]',
              
              /* 본인 강조: 테두리(그림자)만 유지 */
              { 'shadow-[inset_0_0_0_4px_var(--color-butter)] relative z-10': isMyself(item) },
              
              /* 모서리 둥글기 */
              { 'rounded-t-[1.125rem]': index === 0 },
              { 'rounded-b-[1.25rem]': index === rankList.length - 1 }
            ]">
            
            <div class="w-[4rem] text-center">
              <span class="text-[1.75rem] font-['Xcu']" :class="[
                (index % 2 === 0) ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'
              ]">
                {{ item.rank }}
              </span>
            </div>

            <div class="flex-1 text-center px-[1rem]">
              <span class="text-[1.5rem] font-['PfStardust30S'] truncate block" :class="[
                  (index % 2 === 0) ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'
              ]">
                {{ item.nickName }}
                <span v-if="isMyself(item)" class="text-[0.8rem] ml-1 align-top">(나)</span>
              </span>
            </div>

            <div class="w-[6rem] text-right">
              <span class="text-[1.5rem] font-['PfStardust30S']" :class="[
                (index % 2 === 0) ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'
              ]">
                {{ item.score }}p
              </span>
            </div>

            <div class="w-[5rem] ml-[1rem] flex justify-center text-center">
              <TierIcon 
                :tier="item.tier" 
                class="w-[2rem] h-[2rem]" 
              />
            </div>
          </div>
        </div>
    
        <div class="mt-[1.5rem] flex justify-center gap-[1rem] rank-pagination">
          <button @click="changePage(0)" :disabled="currentPage === 0"
            class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform flex items-center justify-center overflow-hidden">
             <div class="flex items-center justify-center -space-x-2.5">
               <LeftArrowIcon class="w-[1.5rem] h-[1.5rem] text-[var(--color-choco)] relative z-10" />
               <LeftArrowIcon class="w-[1.5rem] h-[1.5rem] text-[var(--color-choco)] relative z-0" />
             </div>
          </button>

          <button @click="changePage(Math.max(0, currentPage - 1))" :disabled="currentPage === 0"
            class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform flex items-center justify-center">
            <LeftArrowIcon class="w-[1.5rem] h-[1.5rem] text-[var(--color-choco)]" />
          </button>

          <div class="flex gap-[0.75rem]">
            <button v-for="page in visiblePages" :key="page" @click="changePage(page)"
              class="w-[2rem] h-[2rem] flex items-center justify-center hover:scale-110 transition-transform font-['PfStardust30S'] text-[1.25rem]"
              :class="page === currentPage ? 'text-[var(--color-butter)] drop-shadow-[1px_1px_0_var(--color-choco)]' : 'text-[var(--color-choco)]'">
              {{ page + 1 }}
            </button>
          </div>

          <button @click="changePage(Math.min(totalPages - 1, currentPage + 1))"
            :disabled="currentPage >= totalPages - 1"
            class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform flex items-center justify-center">
            <RightArrowIcon class="w-[1.5rem] h-[1.5rem] text-[var(--color-choco)]" />
          </button>

          <button @click="changePage(totalPages - 1)" :disabled="currentPage >= totalPages - 1"
            class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform flex items-center justify-center overflow-hidden">
            <div class="flex items-center justify-center -space-x-2.5">
               <RightArrowIcon class="w-[1.5rem] h-[1.5rem] text-[var(--color-choco)] relative z-0" />
               <RightArrowIcon class="w-[1.5rem] h-[1.5rem] text-[var(--color-choco)] relative z-10" />
            </div>
          </button>
        </div>
        </div>
      </div>
    </div>

    <GlobalFooter />
  </div>
  </GlobalBackground>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useRanking } from '../logic/useRanking';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import TierIcon from '@/shared/ui/TierIcon.vue';
import LeftArrowIcon from '@/assets/icons/leftArrow.svg?component';
import RightArrowIcon from '@/assets/icons/rightArrow.svg?component';
import GlobalBackground from '@/shared/ui/GlobalBackground.vue';

// [중요] RankItem 인터페이스 임포트 (Ranking.types.ts에 정의된 것)
import type { RankItem } from '../types/ranking.types';

const authStore = useAuthStore();
const router = useRouter();

const displayAvatar = computed(() => myRankInfo.value?.avatar ?? authStore.userInfo?.avatar);
const hasAvatarConfig = computed(() => {
  if (!displayAvatar.value) return false;
  return Object.values(displayAvatar.value).some((value) => Boolean(value));
});

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

onMounted(() => {
  if (authStore.isLoggedIn && !authStore.userInfo) {
    authStore.fetchUserInfo();
  }
});

// [타입 대응] 본인 확인 로직
// item.email과 authStore의 이메일을 비교
const isMyself = (item: RankItem) => {
  if (!authStore.userInfo?.email) return false;
  return item.email === authStore.userInfo.email;
};

</script>

<style scoped>
.filter-tab {
  transition: transform 140ms ease, box-shadow 140ms ease;
  cursor: pointer;
}

.tab-active {
  transform: translateY(-2px);
  box-shadow: 6px 6px 0px 0px var(--color-choco);
}

.tab-inactive {
  transform: translateY(2px);
  box-shadow: 2px 2px 0px 0px var(--color-choco);
}

@media (max-width: 1280px) {
  .rank-content {
    overflow-x: hidden;
  }

  .rank-shell {
    overflow-x: hidden;
    padding-right: 11px;
    padding-left: 10px;
    box-sizing: border-box;
  }

  .rank-avatar {
    display: none;
  }

  .rank-back {
    left: 0;
  }
  .rank-content {
    padding-left: 1.5rem;
    padding-right: 1.5rem;
  }

  .rank-panel {
    max-width: 100%;
    width: 100%;
    margin-left: 0;
    transform: none !important;
  }

  .rank-panel-header {
    flex-direction: row;
    align-items: center;
    gap: 12px;
  }

  .rank-tabs {
    flex: 1 1 auto;
  }

  .rank-search {
    width: min(38vw, 12rem);
  }

  .rank-table {
    height: auto;
    max-height: none;
    overflow-y: visible;
  }

  .rank-row {
    height: auto;
    padding-top: 0.75rem;
    padding-bottom: 0.75rem;
  }

  .rank-pagination {
    gap: 0.5rem;
  }

  .rank-tabs span {
    font-size: 1.2rem !important;
  }

  .rank-row span {
    font-size: 1.1rem !important;
  }

  .rank-search input {
    font-size: 1rem !important;
  }
}
</style>
