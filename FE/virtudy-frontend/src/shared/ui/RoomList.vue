<template>
  <div class="h-[686px] w-[691px]">
    <div class="h-[62px] relative mb-[20px]">
      <div class="absolute left-[31px] top-[9px] flex gap-0">
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
        class="absolute right-0 top-[14px] border-2 border-[var(--color-choco)] border-solid px-[18px] py-[6px] rounded-[20px] bg-[var(--color-butter2)] shadow-[4px_4px_0px_0px_var(--color-choco)] hover:scale-105 transition-transform"
      >
        <span class="text-[var(--color-choco)] text-[18px] font-['PfStardust30S'] font-normal leading-none">
          최애방 선택하기
        </span>
      </button>
      
      <div 
        v-if="currentFilter === 'all'"
        class="absolute right-0 top-[14px] w-[219px] border-2 border-[var(--color-choco)] border-solid bg-[var(--color-cream2)] h-[33px] flex items-center px-[7px] gap-[10px]" 
        style="box-shadow: 3.667px 3.667px 0px 0px var(--color-choco);"
      >
        <div class="w-[21px] h-[21px] flex items-center justify-center">
          <svg viewBox="0 0 21 21" class="w-[19.636px] h-[19.636px]">
            <path d="M8 2C11.314 2 14 4.686 14 8C14 9.248 13.587 10.397 12.897 11.324L18.707 17.071C19.098 17.461 19.098 18.095 18.707 18.485C18.317 18.876 17.683 18.876 17.293 18.485L11.486 12.678C10.559 13.368 9.41 13.781 8.162 13.781C4.848 13.781 2.162 11.095 2.162 7.781C2.162 4.467 4.848 1.781 8.162 1.781Z" fill="var(--color-choco)"/>
          </svg>
        </div>
        <input 
          v-model="searchQuery"
          type="text" 
          placeholder="Search"
          class="flex-1 bg-transparent border-none outline-none text-[var(--color-syrup)] text-[18px] font-['PfStardust30S'] font-normal leading-normal tracking-[-0.7px]"
          @input="onSearch"
        />
      </div>
    </div>
    
    <div class="bg-[var(--color-cream2)] border-2 border-[var(--color-choco)] border-solid h-[589px] rounded-[20px] p-[20px] overflow-clip" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
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

            <div v-if="room.owner" class="flex gap-[8px] mr-[10px] z-10">
              <button 
                @click.stop="emit('edit', room)" 
                class="w-[24px] h-[24px] flex items-center justify-center hover:scale-110 transition-transform bg-[var(--color-cream)] rounded-full border border-[var(--color-choco)]"
                title="방 정보 수정"
              >
                <span class="text-[14px] leading-none">✏️</span>
              </button>
              <button 
                @click.stop="emit('delete', room.roomId!)" 
                class="w-[24px] h-[24px] flex items-center justify-center hover:scale-110 transition-transform bg-[var(--color-cream)] rounded-full border border-[var(--color-choco)]"
                title="방 삭제"
              >
                <span class="text-[12px] leading-none pt-[2px]">🗑️</span>
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
      <div class="flex items-center gap-[24px]">
        <button 
          @click="goToFirstPage" 
          :disabled="currentPage === 1"
          class="w-[28px] h-[28px] relative disabled:opacity-50"
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
          @click="goToPrevPage" 
          :disabled="currentPage === 1"
          class="w-[28px] h-[28px] relative disabled:opacity-50"
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
            v-for="pageNum in visiblePages" 
            :key="pageNum"
            @click="goToPage(pageNum)"
            class="w-[28px] h-[28px] flex items-center justify-center"
          >
            <span 
              :class="[
                'text-[20px] font-[PfStardust30S] font-normal leading-none tracking-[-0.8px] text-center',
                pageNum === currentPage ? 'text-[var(--color-butter)]' : 'text-[var(--color-choco)]'
              ]"
              :style="pageNum === currentPage ? 'text-shadow: 1px 1px 0px var(--color-butter2);' : 'text-shadow: 1px 1px 0px var(--color-butter2);'"
            >
              {{ pageNum }}
            </span>
          </button>
        </div>
        
        <button 
          @click="goToNextPage" 
          :disabled="currentPage === totalPages"
          class="w-[28px] h-[28px] relative disabled:opacity-50"
        >
          <svg viewBox="0 0 28 28" class="w-full h-full">
            <rect width="28" height="28" fill="var(--color-choco)" opacity="0.3"/>
          </svg>
          <svg viewBox="0 0 20 20" class="absolute inset-1">
            <path d="M8 5L13 10L8 15" stroke="var(--color-choco)" stroke-width="2" fill="none"/>
          </svg>
        </button>
        
        <button 
          @click="goToLastPage" 
          :disabled="currentPage === totalPages"
          class="w-[28px] h-[28px] relative disabled:opacity-50"
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
      
      <div class="absolute right-0 top-[6px]">
        <span class="text-[var(--color-butter2)] text-[20px] font-['PfStardust30S'] font-normal leading-none tracking-[-0.8px]">
          {{ String(Math.min((currentPage - 1) * 6 + 1, totalRooms)).padStart(2, '0') }}-{{ String(Math.min(currentPage * 6, totalRooms)).padStart(2, '0') }} / {{ totalRooms }}
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

// Computed
const totalRooms = computed(() => filteredRooms.value.length);
const totalPages = computed(() => Math.max(1, Math.ceil(totalRooms.value / ITEMS_PER_PAGE)));

const filteredRooms = computed(() => {
  let filtered = [...props.rooms];
  
  if (currentFilter.value === 'myRooms') {
    // 필요한 경우 내 방 필터링 로직 추가
  }
  
  if (currentFilter.value === 'all' && searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(room => 
      room.title.toLowerCase().includes(query)
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
  const start = Math.max(1, currentPage.value - 2);
  const end = Math.min(totalPages.value, currentPage.value + 2);
  
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
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const goToFirstPage = () => {
  currentPage.value = 1;
};

const goToLastPage = () => {
  currentPage.value = totalPages.value;
};

const goToPrevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

const goToNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};

watch(() => props.rooms, () => {
  // currentPage.value = 1; // 필요에 따라 주석 해제
});
</script>