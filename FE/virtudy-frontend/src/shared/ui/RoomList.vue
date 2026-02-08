<template>
  <div class="h-[686px] w-[691px]">
    <div class="h-[62px] relative mb-[20px]">
      <div class="absolute left-[31px] top-[25px] flex gap-0 z-0">
        <button 
          @click="setFilter('all')"
          :class="[
            'filter-tab border-2 border-[var(--color-choco)] border-solid px-[32px] py-[20px] rounded-tl-[30px] rounded-tr-[30px] rounded-bl-[2px] rounded-br-[2px]',
            effectiveFilter === 'all' ? 'bg-[var(--color-butter)] tab-active' : 'bg-[var(--color-cream)] tab-inactive'
          ]"
          :disabled="props.tutorialMode"
        >
          <span class="text-[var(--color-choco)] text-[24px] font-[PfStardust30S] font-normal leading-none">
            전체
          </span>
        </button>
        <button 
          @click="setFilter('myRooms')"
          :class="[
            'filter-tab border-2 border-[var(--color-choco)] border-solid px-[32px] py-[20px] rounded-tl-[30px] rounded-tr-[30px] rounded-bl-[2px] rounded-br-[2px]g ml-[-2px]',
            currentFilter === 'myRooms' ? 'bg-[var(--color-butter)] tab-active' : 'bg-[var(--color-cream)] tab-inactive'
          ]"
          :disabled="props.tutorialMode"
        >
          <span class="text-[var(--color-choco)] text-[24px] font-[PfStardust30S] font-normal leading-none">
            내 방
          </span>
        </button>
      </div>

      <button
        v-if="effectiveFilter === 'myRooms'"
        @click="toggleFavoriteSelect"
        class="favorite-select-btn absolute right-3 top-[40px] h-[2.0625rem] border-2 border-[var(--color-choco)] border-solid px-[18px] py-[6px] rounded-[20px] bg-[var(--color-butter2)] shadow-[4px_4px_0px_0px_var(--color-choco)] hover:scale-105 transition-transform"
        :disabled="props.tutorialMode"
      >
        <span class="text-[var(--color-choco)] text-[20px] font-[PfStardust30S] font-normal leading-none inline-flex items-center translate-y-[-2px]">
          최애방 선택하기
        </span>
      </button>
      
      <div 
        v-if="effectiveFilter === 'all'"
        class="absolute right-3 top-[40px] w-[219px] border-2 border-[var(--color-choco)] border-solid bg-[var(--color-cream2)] flex items-center h-[2.0625rem] p-[0.45831rem] gap-[0.625rem]" 
        style="box-shadow: 3.667px 3.667px 0px 0px var(--color-choco);"
      >
        <div class="w-[21px] h-[21px] flex items-center justify-center">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M18.0001 16.3637V15.5455H17.1819V14.7273H16.3637V13.9091H15.5455V13.0909H13.9092V12.2728H14.7274V10.6364H15.5455V5.72728H14.7274V4.09092H13.9092V3.27273H13.091V2.45455H12.2728V1.63637H10.6364V0.818184H5.72734V1.63637H4.09097V2.45455H3.27279V3.27273H2.4546V4.09092H1.63642V5.72728H0.818237V10.6364H1.63642V12.2728H2.4546V13.0909H3.27279V13.9091H4.09097V14.7273H5.72734V15.5455H10.6364V14.7273H12.2728V13.9091H13.091V15.5455H13.9092V16.3637H14.7274V17.1819H15.5455V18H16.3637V18.8182H18.0001V18H18.8183V16.3637H18.0001ZM9.81825 12.2728V13.0909H6.54552V12.2728H4.90915V11.4546H4.09097V9.8182H3.27279V6.54547H4.09097V4.9091H4.90915V4.09092H6.54552V3.27273H9.81825V4.09092H11.4546V4.9091H12.2728V6.54547H13.091V9.8182H12.2728V11.4546H11.4546V12.2728H9.81825Z" fill="#805143"/>
          </svg>
        </div>
        <input 
          v-model="searchQuery"
          type="text" 
          placeholder="방 제목, 코드를 입력하세요"
          spellcheck="false"
          class="flex-1 min-w-0 truncate bg-transparent border-none outline-none text-[var(--color-choco)] text-[18px] font-[PfStardust30S] font-normal leading-normal tracking-[-0.7px]"
          @input="onSearch"
          @paste="onSearch"
        />
      </div>
    </div>
    
    <div class="relative z-10 bg-[var(--color-cream2)] border-2 border-[var(--color-choco)] border-solid h-[589px] rounded-[20px] p-[20px] overflow-clip" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
      <div class="grid grid-cols-2 gap-[20px] h-full">
        <div 
          v-for="(room, index) in displayedRooms" 
          :key="`room-${room.roomId || index}`"
          :class="[
            'border-2 border-[var(--color-choco)] border-solid rounded-[20px] relative cursor-pointer hover:scale-105 transition-transform w-[315.5px] h-full group',
            isMyRoomTab && room.owner ? 'bg-[var(--color-butter)]' : 'bg-[var(--color-butter2)]',
            index === 0 ? 'first-room-card' : ''
          ]"
          style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
          @click="onRoomClick(room)"
        >
          <div v-if="room.lockIcon" class="absolute left-[16px] top-[16px] z-20">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M20 12V11H19V6H18V4H17V3H16V2H14V1H10V2H8V3H7V4H6V6H5V11H4V12H3V22H4V23H20V22H21V12H20ZM8 6H9V5H10V4H14V5H15V6H16V11H8V6Z" fill="#805143"/>
            </svg>
          </div>

          <div class="absolute right-[20px] top-[20px] flex items-center">
            <div v-if="room.owner" class="flex gap-[10px] mr-[10px] z-10">
              <button 
                @click.stop="emit('edit', room)" 
                :class="['text-[22px] font-[PfStardust30S] font-normal leading-none', isMyRoomTab && room.owner ? 'text-[#BF7D4C]' : 'text-[var(--color-syrup)]']"
                title="방 정보 수정"
              >
                수정
              </button>
              <button 
                @click.stop="emit('delete', room.roomId!)" 
                :class="['text-[22px] font-[PfStardust30S] font-normal leading-none', isMyRoomTab && room.owner ? 'text-[#BF7D4C]' : 'text-[var(--color-syrup)]']"
                title="방 삭제"
              >
                삭제
              </button>
            </div>
            <p :class="['text-[24px] font-[PfStardust30S] font-normal leading-none', isMyRoomTab && room.owner ? 'text-[#BF7D4C]' : 'text-[var(--color-syrup)]']">
              {{ room.currentMembers || 0 }}/{{ room.maxMembers || 6 }}
            </p>
          </div>

          <button 
            v-if="(isMyRoomTab && selectingFavorite) || room.favorite" 
            @click.stop="onFavoriteClick(room.roomId!)"
            class="absolute right-[20px] bottom-[10px] w-[24px] h-[24px] flex items-center justify-center hover:scale-110 transition-transform z-20"
            title="최애 스터디방 설정"
          >
            <div class="w-full h-full" v-html="getHeartSvg(room.favorite)"></div>
          </button>
          
          <div class="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-full px-[24px]">
            <div class="flex flex-col items-center justify-center">
              <p
                class="text-[var(--color-choco)] text-[32px] font-['Xcu'] font-medium leading-normal text-center break-words w-full h-auto min-h-[3rem]"
                style="word-break: keep-all; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;"
              >
                {{ room.title || '방제목' }}
              </p>
            </div>
          </div>
        </div>
        
        <div 
          v-for="index in Math.max(0, 6 - displayedRooms.length)" 
          :key="`empty-${index}`"
          class="bg-[var(--color-butter2)] border-2 border-[var(--color-choco)] border-solid rounded-[20px] relative opacity-30 w-[315.5px] h-full"
          style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
        >
          <div class="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2">
            <p class="text-[var(--color-choco)] text-[32px] font-['Xcu'] font-medium leading-none text-center">
              빈 방
            </p>
          </div>
        </div>
      </div>
    </div>
    
    <div class="h-[28px] mt-[15px] flex items-center justify-center relative">
      <div class="flex items-center gap-[1rem]">
        <button 
          @click="goToFirstPage" 
          :disabled="currentPage === 1 || !hasRooms"
          class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform flex items-center justify-center overflow-hidden"
        >
          <div class="flex items-center justify-center -space-x-2.5">
            <LeftArrowIcon class="w-[1.5rem] h-[1.5rem] text-[var(--color-choco)] relative z-10" />
            <LeftArrowIcon class="w-[1.5rem] h-[1.5rem] text-[var(--color-choco)] relative z-0" />
          </div>
        </button>
        
        <button 
          @click="goToPrevPage" 
          :disabled="currentPage === 1 || !hasRooms"
          class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform flex items-center justify-center"
        >
          <LeftArrowIcon class="w-[1.5rem] h-[1.5rem] text-[var(--color-choco)]" />
        </button>
        
        <div class="flex items-center gap-[0.75rem]">
          <button 
            v-for="pageNum in visiblePages" 
            :key="pageNum"
            @click="goToPage(pageNum)"
            class="w-[2rem] h-[2rem] flex items-center justify-center hover:scale-110 transition-transform font-[PfStardust30S] text-[1.25rem]"
            :class="[
              pageNum === currentPage ? 'text-[var(--color-butter)] drop-shadow-[1px_1px_0_var(--color-choco)]' : 'text-[var(--color-choco)]',
              (!hasRooms || pageNum > maxNavigablePage) ? 'pointer-events-none opacity-30' : ''
            ]"
          >
            {{ pageNum }}
          </button>
        </div>
        
        <button 
          @click="goToNextPage" 
          :disabled="currentPage >= maxNavigablePage || !hasRooms"
          class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform flex items-center justify-center"
        >
          <RightArrowIcon class="w-[1.5rem] h-[1.5rem] text-[var(--color-choco)]" />
        </button>
        
        <button 
          @click="goToLastPage" 
          :disabled="currentPage >= maxNavigablePage || !hasRooms"
          class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform flex items-center justify-center overflow-hidden"
        >
          <div class="flex items-center justify-center -space-x-2.5">
            <RightArrowIcon class="w-[1.5rem] h-[1.5rem] text-[var(--color-choco)] relative z-0" />
            <RightArrowIcon class="w-[1.5rem] h-[1.5rem] text-[var(--color-choco)] relative z-10" />
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import LeftArrowIcon from '@/assets/icons/leftArrow.svg?component';
import RightArrowIcon from '@/assets/icons/rightArrow.svg?component';

interface Room {
  roomId?: string;
  id?: number;
  title: string;
  currentMembers: number;
  maxMembers: number;
  createdAt: string;
  owner?: boolean;
  description?: string;
  favorite?: boolean;
  lockIcon?: boolean;
}

interface RoomListProps {
  rooms?: Room[];
  isMyRoomTab?: boolean;
  tutorialMode?: boolean;
  tutorialTab?: 'all' | 'myRooms';
}

const props = withDefaults(defineProps<RoomListProps>(), {
  rooms: () => [],
  tutorialMode: false,
  tutorialTab: 'all'
});

const emit = defineEmits<{
  roomClick: [room: Room];
  filterChange: [filter: string];
  search: [query: string];
  edit: [room: Room];
  delete: [roomId: string];
  toggleFavorite: [roomId: string];
}>();


const currentFilter = ref<string>('all');
const selectingFavorite = ref(false);
const searchQuery = ref<string>('');
const currentPage = ref<number>(1);
const ITEMS_PER_PAGE = 6;


// 튜토리얼 모드일 때 탭 상태 동기화
const effectiveFilter = computed(() => {
  return props.tutorialMode ? props.tutorialTab : currentFilter.value;
});

// SVG 데이터 분리
// 1. 테두리 (항상 표시될 부분)
const outlineVector = `
<g id="Vector_4">
<path d="M0 4H2V6H0V4Z" fill="#805143"/>
<path d="M0 6H2V8H0V6Z" fill="#805143"/>
<path d="M2 8H4V10H2V8Z" fill="#805143"/>
<path d="M4 10H6V12H4V10Z" fill="#805143"/>
<path d="M8 10H10V12H8V10Z" fill="#805143"/>
<path d="M10 8H12V10H10V8Z" fill="#805143"/>
<path d="M12 6H14V8H12V6Z" fill="#805143"/>
<path d="M12 4H14V6H12V4Z" fill="#805143"/>
<path d="M12 2H14V4H12V2Z" fill="#805143"/>
<path d="M10 0H12V2H10V0Z" fill="#805143"/>
<path d="M8 0H10V2H8V0Z" fill="#805143"/>
<path d="M6 2H8V4H6V2Z" fill="#805143"/>
<path d="M4 0H6V2H4V0Z" fill="#805143"/>
<path d="M2 0H4V2H2V0Z" fill="#805143"/>
<path d="M0 2H2V4H0V2Z" fill="#805143"/>
<path d="M0 2H2V4H0V2Z" fill="#805143"/>
<path d="M6 12H8V14H6V12Z" fill="#805143"/>
</g>`;

// 2. 채움 (선택되었을 때만 표시될 부분)
const fillVectors = `
<g id="Vector">
<path d="M10 2H12V4H10V2Z" fill="#FFF8E5"/>
<path d="M4 2H6V4H4V2Z" fill="#FFF8E5"/>
</g>
<g id="Vector_2">
<path d="M2 2H4V4H2V2Z" fill="#FFD966"/>
<path d="M2 4H4V6H2V4Z" fill="#FFD966"/>
<path d="M4 6H6V8H4V6Z" fill="#FFD966"/>
<path d="M6 8H8V10H6V8Z" fill="#FFD966"/>
<path d="M8 8H10V10H8V8Z" fill="#FFD966"/>
<path d="M8 6H10V8H8V6Z" fill="#FFD966"/>
<path d="M10 6H12V8H10V6Z" fill="#FFD966"/>
<path d="M10 4H12V6H10V4Z" fill="#FFD966"/>
<path d="M8 4H10V6H8V4Z" fill="#FFD966"/>
<path d="M6 4H8V6H6V4Z" fill="#FFD966"/>
<path d="M4 4H6V6H4V4Z" fill="#FFD966"/>
<path d="M6 6H8V8H6V6Z" fill="#FFD966"/>
<path d="M8 2H10V4H8V2Z" fill="#FFD966"/>
</g>
<g id="Vector_3">
<path d="M2 6H4V8H2V6Z" fill="#EBAB6A"/>
<path d="M4 8H6V10H4V8Z" fill="#EBAB6A"/>
<path d="M6 10H8V12H6V10Z" fill="#EBAB6A"/>
</g>`;

// SVG 문자열 생성 함수
const getHeartSvg = (isFavorite: boolean | undefined) => {
  // 선택됨(favorite): 테두리 + 내부 색상 모두 표시
  // 선택안됨(!favorite): 테두리만 표시
  const content = isFavorite ? fillVectors + outlineVector : outlineVector;
  return `<svg width="100%" height="100%" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg">${content}</svg>`;
};



const totalRooms = computed(() => filteredRooms.value.length);
const hasRooms = computed(() => totalRooms.value > 0);
const maxNavigablePage = computed(() => Math.max(1, Math.ceil(totalRooms.value / ITEMS_PER_PAGE)));

const filteredRooms = computed(() => {
  let filtered = [...props.rooms];
  
  if (effectiveFilter.value === 'all' && searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(room => 
      room.title.toLowerCase().includes(query) ||
      (room.roomId ?? '').toLowerCase().includes(query)
    );
  }
  
  return filtered;
});

const displayedRooms = computed(() => {
  const start = (currentPage.value - 1) * ITEMS_PER_PAGE;
  const end = start + ITEMS_PER_PAGE;
  return filteredRooms.value.slice(start, end);
});

const visiblePages = computed(() => {
  const pages: number[] = [];
  const windowSize = 5;
  const total = maxNavigablePage.value;
  
  let start = currentPage.value - Math.floor(windowSize / 2);
  let end = start + windowSize - 1;

  if (start < 1) {
    start = 1;
    end = Math.min(total, windowSize);
  }

  if (end > total) {
    end = total;
    start = Math.max(1, end - windowSize + 1);
  }

  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  return pages;
});

// Methods
const setFilter = (filter: string) => {
  if (props.tutorialMode) return; // 튜토리얼 모드에서는 무시
  currentFilter.value = filter;
  currentPage.value = 1;
  if (filter !== 'myRooms') {
    selectingFavorite.value = false;
  }
  emit('filterChange', filter);
};

const onSearch = () => {
  if (props.tutorialMode) return; // 튜토리얼 모드에서는 무시
  if (currentFilter.value !== 'all') {
    currentFilter.value = 'all';
    emit('filterChange', 'all');
  }
  currentPage.value = 1;
  emit('search', searchQuery.value);
};

const onRoomClick = (room: Room) => {
  emit('roomClick', room);
};

const toggleFavoriteSelect = () => {
  selectingFavorite.value = !selectingFavorite.value;
};

const onFavoriteClick = (roomId: string) => {
  if (!selectingFavorite.value) return;
  emit('toggleFavorite', roomId);
  selectingFavorite.value = false;
};

const goToPage = (page: number) => {
  if (!hasRooms.value) return;
  if (page >= 1 && page <= maxNavigablePage.value) {
    currentPage.value = page;
  }
};

const goToFirstPage = () => {
  if (!hasRooms.value) return;
  currentPage.value = 1;
};

const goToLastPage = () => {
  if (!hasRooms.value) return;
  currentPage.value = maxNavigablePage.value;
};

const goToPrevPage = () => {
  if (!hasRooms.value) return;
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

const goToNextPage = () => {
  if (!hasRooms.value) return;
  if (currentPage.value < maxNavigablePage.value) {
    currentPage.value++;
  }
};

watch(() => props.rooms, () => {
  if (!hasRooms.value) {
    currentPage.value = 1;
    return;
  }
  if (currentPage.value > maxNavigablePage.value) {
    currentPage.value = maxNavigablePage.value;
  }
});
</script>

<style scoped>
.filter-tab {
  transition: transform 140ms ease, box-shadow 140ms ease;
}

.tab-active {
  transform: translateY(-2px);
  box-shadow: 6px 6px 0px 0px var(--color-choco);
}

.tab-inactive {
  transform: translateY(2px);
  box-shadow: 2px 2px 0px 0px var(--color-choco);
}
</style>
