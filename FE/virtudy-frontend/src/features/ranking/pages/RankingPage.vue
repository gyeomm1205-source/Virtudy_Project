<template>
  <div class="min-h-screen bg-[var(--color-syrup)] relative w-full flex flex-col">
    <GlobalNavBar />

    <div
      class="absolute left-[30rem] top-[20.3rem] z-10"
      :style="{ width: '14rem', height: '16rem' }"
    >
      <CharacterAvatar
        v-if="hasAvatarConfig"
        :config="displayAvatar!"
        class="w-full h-full"
      />
    </div>

    <div class="flex-1 flex flex-col pt-[4.5rem] pb-[6.5rem] w-full min-h-[calc(100vh-200px)] px-[4.75rem]">
      <div class="w-full max-w-[74rem] mx-auto relative">
        <button @click="goBack"
          class="absolute -left-13 top-[2rem] w-[4rem] h-[4rem] cursor-pointer hover:scale-110 transition-transform">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
            stroke="var(--color-choco)" class="w-full h-full">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
          </svg>
        </button>

        <div class="flex items-end justify-start gap-[3.5rem] mt-[4.5rem] mb-[1rem]">
          <!-- 랭킹 제목 -->
          <h1 class="text-[var(--color-pancake)] text-[clamp(4rem,9.75rem,9.75rem)] font-['Ram'] font-medium leading-none tracking-[-1.17rem] mt-[6.5rem] -ml-[4rem]">
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

              <div class="text-[var(--color-butter)] font-['Xcu'] font-normal drop-shadow-md mt-[-0.5rem] translate-x-[14rem]">
                <template v-if="myRankInfo?.rank">
                  <span class="leading-none text-[clamp(3rem,8.125rem,8.125rem)]">
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
        <div class="max-w-[61.375rem] w-full ml-26 mr-0 relative mb-[2rem] translate-x-[0.5rem]">

        <div class="flex justify-between items-end mb-[-2px] relative z-10 px-[2rem]">
          <div class="flex items-center gap-[1rem]">
            <div class="flex gap-0 ">
              <button @click="changeType('private')" :class="[
                'border-2 border-[var(--color-choco)] border-solid px-[32px] py-[10px] rounded-tl-[30px] rounded-tr-[30px] rounded-bl-[2px] rounded-br-[2px]',
                rankType === 'private' ? 'bg-[var(--color-butter)]' : 'bg-[var(--color-cream)]'
              ]" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
                <span class="text-[var(--color-choco)] text-[1.5rem] font-['PfStardust30S']">개인</span>
              </button>
              <button @click="changeType('team')" :class="[
                'border-2 border-[var(--color-choco)] border-solid px-[32px] py-[10px] rounded-tl-[30px] rounded-tr-[30px] rounded-bl-[2px] rounded-br-[2px] ml-[-2px]',
                rankType === 'team' ? 'bg-[var(--color-butter)]' : 'bg-[var(--color-cream)]'
              ]" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
                <span class="text-[var(--color-choco)] text-[1.5rem] font-['PfStardust30S']">팀</span>
              </button>
            </div>
          </div>

          <div
            class="w-[min(24.5625rem,40vw)] h-[2.0625rem] border-2 border-[var(--color-choco)] bg-[var(--color-cream2)] flex items-center px-[0.5rem] gap-[0.5rem] mb-[0.5rem] shadow-[4px_4px_0px_0px_var(--color-choco)]">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 20 20" fill="none" class="w-[1.25rem] h-[1.25rem]">
              <path d="M18 16.3637V15.5455H17.1818V14.7273H16.3636V13.9091H15.5454V13.0909H13.909V12.2727H14.7272V10.6364H15.5454V5.72728H14.7272V4.09091H13.909V3.27273H13.0909V2.45454H12.2727V1.63636H10.6363V0.818176H5.72722V1.63636H4.09085V2.45454H3.27267V3.27273H2.45448V4.09091H1.6363V5.72728H0.818115V10.6364H1.6363V12.2727H2.45448V13.0909H3.27267V13.9091H4.09085V14.7273H5.72722V15.5455H10.6363V14.7273H12.2727V13.9091H13.0909V15.5455H13.909V16.3637H14.7272V17.1818H15.5454V18H16.3636V18.8182H18V18H18.8181V16.3637H18ZM9.81813 12.2727V13.0909H6.5454V12.2727H4.90903V11.4546H4.09085V9.81819H3.27267V6.54546H4.09085V4.90909H4.90903V4.09091H6.5454V3.27273H9.81813V4.09091H11.4545V4.90909H12.2727V6.54546H13.0909V9.81819H12.2727V11.4546H11.4545V12.2727H9.81813Z" fill="#805143"/>
            </svg>
            <input v-model="searchKeyword" @input="handleSearch" @paste="handleSearch" @keyup.enter="handleSearch" type="text" placeholder="Search"
              class="flex-1 bg-transparent border-none outline-none text-[var(--color-syrup)] text-[1.125rem] font-['PfStardust30S'] placeholder-[var(--color-syrup)] opacity-70" />
          </div>
        </div>

        <div
          class="border-2 border-[var(--color-choco)] w-full h-[37.5rem] rounded-[1.25rem] overflow-hidden bg-[var(--color-cream)] flex flex-col relative z-0 shadow-[4px_4px_0px_0px_var(--color-choco)]">

          <div v-if="isLoading"
            class="flex-1 flex items-center justify-center text-[1.5rem] font-['PfStardust30S'] text-[var(--color-choco)]">
            Loading...
          </div>
          <div v-else-if="rankList.length === 0"
            class="flex-1 flex items-center justify-center text-[1.5rem] font-['PfStardust30S'] text-[var(--color-choco)]">
            랭킹 데이터가 없습니다.
          </div>

          <div v-else v-for="(item, index) in rankList" :key="item.id"
            class="flex items-center h-[3.75rem] border-b border-[var(--color-syrup)] last:border-none px-[2rem]"
            :class="[
              index % 2 === 0 ? 'bg-[var(--color-choco)]' : 'bg-[var(--color-cream)]',
              { '!bg-[var(--color-choco)] !border-2 !border-[var(--color-butter)] box-border': isMyself(item) }
            ]">
            <div class="w-[4rem] text-center">
              <span class="text-[1.75rem] font-['Xcu']" :class="[
                isMyself(item) ? 'text-[var(--color-butter)]' :
                  (index % 2 === 0) ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'
              ]">
                {{ item.rank }}
              </span>
            </div>

            <div class="flex-1 text-center px-[1rem]">
              <span class="text-[1.5rem] font-['PfStardust30S'] truncate block" :class="[
                isMyself(item) ? 'text-[var(--color-butter)]' :
                  (index % 2 === 0 && index < 3) ? 'text-[var(--color-butter)]' :
                    (index === 1) ? 'text-[var(--color-butter)]' :
                      'text-[var(--color-pancake)]'
              ]">
                {{ item.nickName }}
                <span v-if="isMyself(item)" class="text-[0.8rem] ml-1 align-top">(나)</span>
              </span>
            </div>

            <div class="w-[6rem] text-right">
              <span class="text-[1.5rem] font-['PfStardust30S']" :class="[
                isMyself(item) ? 'text-[var(--color-butter)]' :
                  (index % 2 === 0) ? 'text-[var(--color-cream)]' : 'text-[var(--color-pancake)]'
              ]">
                {{ item.score }}p
              </span>
            </div>

            <div class="w-[5rem] ml-[1rem] flex justify-center text-center">
              <span 
                class="text-[1.25rem] font-['PfStardust30S'] transition-colors"
                :class="getTierColorClass(item.tier)"
              >
                {{ item.tier }}
              </span>
            </div>
          </div>
        </div>

        <div class="mt-[1.5rem] flex justify-center gap-[1rem]">
          <button @click="changePage(0)" :disabled="currentPage === 0"
            class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform">
            <svg viewBox="0 0 20 20" class="w-full h-full fill-[var(--color-choco)]">
              <path d="M13 5L8 10L13 15L12 16L6 10L12 4L13 5Z" />
              <path d="M9 5L4 10L9 15L8 16L2 10L8 4L9 5Z" />
            </svg>
          </button>
          <button @click="changePage(Math.max(0, currentPage - 1))" :disabled="currentPage === 0"
            class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform">
            <svg viewBox="0 0 20 20" class="w-full h-full fill-[var(--color-choco)]">
              <path d="M13 5L8 10L13 15L12 16L6 10L12 4L13 5Z" />
            </svg>
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
            class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform">
            <svg viewBox="0 0 20 20" class="w-full h-full fill-[var(--color-choco)]">
              <path d="M7 5L12 10L7 15L8 16L14 10L8 4L7 5Z" />
            </svg>
          </button>
          <button @click="changePage(totalPages - 1)" :disabled="currentPage >= totalPages - 1"
            class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform">
            <svg viewBox="0 0 20 20" class="w-full h-full fill-[var(--color-choco)]">
              <path d="M7 5L12 10L7 15L8 16L14 10L8 4L7 5Z" />
              <path d="M11 5L16 10L11 15L12 16L18 10L12 4L11 5Z" />
            </svg>
          </button>
        </div>
        </div>
      </div>
    </div>

    <GlobalFooter />
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useRanking } from '../logic/useRanking';
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';

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

// [추가] 티어별 텍스트 색상 클래스 반환 함수(나중에 이미지로 바꿀 예정)
const getTierColorClass = (tierName: string | undefined) => {
  const tier = tierName?.toUpperCase() || '';

  if (tier.includes('BRONZE')) {
    return 'text-[#CD7F32]';
  }
  if (tier.includes('SILVER')) {
    return 'text-[#C0C0C0]';
  }
  if (tier.includes('GOLD')) {
    return 'text-[#FFD700]';
  }
  if (tier.includes('PLATINUM')) {
    return 'text-[#00CED1]';
  }
  if (tier.includes('DIA')) {
    return 'text-[#38BDF8]';
  }
  
  return 'text-[var(--color-choco)]';
};
</script>