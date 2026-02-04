<template>
  <div class="h-[686px] w-[691px]">
    <div class="h-[62px] relative mb-[20px]">
      <div class="absolute left-[31px] top-[25px] flex gap-0 z-0">
        <button 
          @click="setFilter('all')"
          :class="[
            'border-2 border-[var(--color-choco)] border-solid px-[32px] py-[20px] rounded-tl-[30px] rounded-tr-[30px] rounded-bl-[2px] rounded-br-[2px]',
            currentFilter === 'all' ? 'bg-[var(--color-butter)]' : 'bg-[var(--color-cream)]'
          ]"
          style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
        >
          <span class="text-[var(--color-choco)] text-[24px] font-['PfStardust30S'] font-normal leading-none">
            전체
          </span>
        </button>
        <button 
          @click="setFilter('myRooms')"
          :class="[
            'border-2 border-[var(--color-choco)] border-solid px-[32px] py-[20px] rounded-tl-[30px] rounded-tr-[30px] rounded-bl-[2px] rounded-br-[2px]',
            currentFilter === 'myRooms' ? 'bg-[var(--color-butter)]' : 'bg-[var(--color-cream)]'
          ]"
          style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
        >
          <span class="text-[var(--color-choco)] text-[24px] font-['PfStardust30S'] font-normal leading-none">
            내 방
          </span>
        </button>
      </div>

      <button
        v-if="currentFilter === 'myRooms'"
        @click="toggleFavoriteSelect"
        class="absolute right-3 top-[37px] border-2 border-[var(--color-choco)] border-solid px-[18px] py-[6px] rounded-[20px] bg-[var(--color-butter2)] shadow-[4px_4px_0px_0px_var(--color-choco)] hover:scale-105 transition-transform"
      >
        <span class="text-[var(--color-choco)] text-[18px] font-['PfStardust30S'] font-normal leading-none">
          최애방 선택하기
        </span>
      </button>
      
      <div 
        v-if="currentFilter === 'all'"
        class="absolute right-3 top-[35px] w-[219px] border-2 border-[var(--color-choco)] border-solid bg-[var(--color-cream2)] flex items-center h-[2.0625rem] p-[0.45831rem] gap-[0.625rem]" 
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
          autocapitalize="off"
          autocomplete="off"
          autocorrect="off"
          class="flex-1 min-w-0 truncate bg-transparent border-none outline-none text-[var(--color-syrup)] text-[18px] font-['PfStardust30S'] font-normal leading-normal tracking-[-0.7px]"
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
          class="bg-[var(--color-butter2)] border-2 border-[var(--color-choco)] border-solid rounded-[20px] relative cursor-pointer hover:scale-105 transition-transform w-[315.5px] h-full group"
          style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
          @click="onRoomClick(room)"
        >
          <div class="absolute right-[20px] top-[20px] flex items-center">
            <button 
              v-if="isMyRoomTab && (selectingFavorite || room.favorite)" 
              @click.stop="onFavoriteClick(room.roomId!)"
              class="w-[24px] h-[24px] flex items-center justify-center hover:scale-110 transition-transform mr-[8px] z-20"
              title="최애 스터디방 설정"
              style="display: none;"
            >
              <svg v-if="room.favorite" viewBox="0 0 24 24" class="w-full h-full fill-[var(--color-choco)]">
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" class="w-full h-full fill-none stroke-[var(--color-choco)] stroke-2">
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
              </svg>
            </button>

            <div v-if="room.owner" class="flex gap-[10px] mr-[10px] z-10">
              <button 
                @click.stop="emit('edit', room)" 
                class="text-[var(--color-syrup)] text-[22px] font-['PfStardust30S'] font-normal leading-none"
                title="방 정보 수정"
              >
                수정
              </button>
              <button 
                @click.stop="emit('delete', room.roomId!)" 
                class="text-[var(--color-syrup)] text-[22px] font-['PfStardust30S'] font-normal leading-none"
                title="방 삭제"
              >
                삭제
              </button>
            </div>

            <p class="text-[var(--color-syrup)] text-[24px] font-['PfStardust30S'] font-normal leading-none">
              {{ room.currentMembers || 0 }}/{{ room.maxMembers || 6 }}
            </p>
          </div>

          <button 
            v-if="isMyRoomTab && (selectingFavorite || room.favorite)" 
            @click.stop="onFavoriteClick(room.roomId!)"
            class="absolute right-[20px] bottom-[20px] w-[24px] h-[24px] flex items-center justify-center hover:scale-110 transition-transform z-20"
            title="최애 스터디방 설정"
          >
            <svg v-if="room.favorite" viewBox="0 0 24 24" class="w-full h-full fill-[var(--color-choco)]">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" class="w-full h-full fill-none stroke-[var(--color-choco)] stroke-2">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
            </svg>
          </button>
          
          <div class="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[90%]">
            <p class="text-[var(--color-choco)] text-[32px] font-['Xcu'] font-medium leading-tight text-center truncate">
              {{ room.title || '방제목' }}
            </p>
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
          class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform"
        >
          <svg viewBox="0 0 20 20" class="w-full h-full fill-[var(--color-choco)]">
            <path d="M13 5L8 10L13 15L12 16L6 10L12 4L13 5Z" />
            <path d="M9 5L4 10L9 15L8 16L2 10L8 4L9 5Z" />
          </svg>
        </button>
        
        <button 
          @click="goToPrevPage" 
          :disabled="currentPage === 1 || !hasRooms"
          class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform"
        >
          <svg viewBox="0 0 20 20" class="w-full h-full fill-[var(--color-choco)]">
            <path d="M13 5L8 10L13 15L12 16L6 10L12 4L13 5Z" />
          </svg>
        </button>
        
        <div class="flex items-center gap-[0.75rem]">
          <button 
            v-for="pageNum in visiblePages" 
            :key="pageNum"
            @click="goToPage(pageNum)"
            class="w-[2rem] h-[2rem] flex items-center justify-center hover:scale-110 transition-transform font-['PfStardust30S'] text-[1.25rem]"
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
          class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform"
        >
          <svg viewBox="0 0 20 20" class="w-full h-full fill-[var(--color-choco)]">
            <path d="M7 5L12 10L7 15L8 16L14 10L8 4L7 5Z" />
          </svg>
        </button>
        
        <button 
          @click="goToLastPage" 
          :disabled="currentPage >= maxNavigablePage || !hasRooms"
          class="w-[2rem] h-[2rem] disabled:opacity-30 hover:scale-110 transition-transform"
        >
          <svg viewBox="0 0 20 20" class="w-full h-full fill-[var(--color-choco)]">
            <path d="M7 5L12 10L7 15L8 16L14 10L8 4L7 5Z" />
            <path d="M11 5L16 10L11 15L12 16L18 10L12 4L11 5Z" />
          </svg>
        </button>
      </div>
      
      <div class="absolute right-0 top-[0px]">
        <span class="text-[var(--color-butter2)] text-[20px] font-['PfStardust30S'] font-normal leading-none tracking-[-0.8px]">
          {{ String(pageStart).padStart(2, '0') }}-{{ String(pageEnd).padStart(2, '0') }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

interface Room {
  roomId?: string;
  id?: number;
  title: string;
  currentMembers: number;
  maxMembers: number;
  createdAt: string;
  owner?: boolean;       // 방장 여부
  description?: string;  // 수정 시 필요할 수 있어서 추가
  favorite?: boolean;    // [NEW] 최애방 여부 추가
}

interface RoomListProps {
  rooms?: Room[];
  isMyRoomTab?: boolean; // [NEW] 내 방 탭인지 여부 prop 추가
}

const props = withDefaults(defineProps<RoomListProps>(), {
  rooms: () => []
});

const emit = defineEmits<{
  roomClick: [room: Room];
  filterChange: [filter: string];
  search: [query: string];
  edit: [room: Room];        // 수정 버튼 클릭
  delete: [roomId: string];  // 삭제 버튼 클릭
  toggleFavorite: [roomId: string]; // [NEW] 하트 클릭 이벤트 추가
}>();

// Reactive state
const currentFilter = ref<string>('all');
const selectingFavorite = ref(false);
const searchQuery = ref<string>('');
const currentPage = ref<number>(1);
const ITEMS_PER_PAGE = 6;
const MAX_PAGES = 20;

// Computed
const totalRooms = computed(() => filteredRooms.value.length);
const totalPages = computed(() => MAX_PAGES);
const hasRooms = computed(() => totalRooms.value > 0);
const maxNavigablePage = computed(() => Math.max(1, Math.ceil(totalRooms.value / ITEMS_PER_PAGE)));
const pageStart = computed(() => (currentPage.value - 1) * ITEMS_PER_PAGE + 1);
const pageEnd = computed(() => Math.min(currentPage.value * ITEMS_PER_PAGE, MAX_PAGES * ITEMS_PER_PAGE));

const filteredRooms = computed(() => {
  let filtered = [...props.rooms];
  
  if (currentFilter.value === 'myRooms') {
    // 필요한 경우 내 방 필터링 로직 추가
  }
  
  if (currentFilter.value === 'all' && searchQuery.value.trim()) {
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
  const total = totalPages.value;
  
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
  currentFilter.value = filter;
  currentPage.value = 1;
  if (filter !== 'myRooms') {
    selectingFavorite.value = false;
  }
  emit('filterChange', filter);
};

const onSearch = () => {
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