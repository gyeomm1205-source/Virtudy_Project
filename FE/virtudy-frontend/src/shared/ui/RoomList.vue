<template>
  <div class="h-[686px] w-[691px]">
    <!-- 상단 필터 및 검색 영역 -->
    <div class="h-[62px] relative mb-[20px]">
      <!-- 필터 버튼들 -->
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
      
      <!-- 정렬 옵션 -->
      <div class="absolute left-[334px] top-[29px] flex gap-[40px]">
        <button 
          @click="setSortBy('popular')"
          :class="[
            'text-[20px] font-[\"PfStardust30S\"] font-normal leading-none tracking-[-0.8px]',
            sortBy === 'popular' ? 'text-[var(--color-cream)]' : 'text-[var(--color-cream)] opacity-70'
          ]"
        >
          사람많은순
        </button>
        <button 
          @click="setSortBy('latest')"
          :class="[
            'text-[20px] font-[\"PfStardust30S\"] font-normal leading-none tracking-[-0.8px]',
            sortBy === 'latest' ? 'text-[var(--color-cream)]' : 'text-[var(--color-cream)] opacity-70'
          ]"
        >
          최신순
        </button>
      </div>
      
      <!-- 검색바 -->
      <div class="absolute right-0 top-[14px] w-[219px] border-2 border-[var(--color-choco)] border-solid bg-[var(--color-cream2)] h-[33px] flex items-center px-[7px] gap-[10px]" style="box-shadow: 3.667px 3.667px 0px 0px var(--color-choco);">
        <div class="w-[21px] h-[21px] flex items-center justify-center">
          <img 
            src="http://localhost:3845/assets/fbefd1e8eda22045729c7ddd65f50e8e857a8a44.svg" 
            alt="검색"
            class="w-[19.636px] h-[19.636px]"
          />
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
    
    <!-- 방 목록 그리드 -->
    <div class="bg-[var(--color-cream2)] border-2 border-[var(--color-choco)] border-solid h-[589px] rounded-[20px] p-[20px] overflow-clip" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
      <div class="grid grid-cols-2 gap-[20px] h-full">
        <div 
          v-for="(room, index) in displayedRooms" 
          :key="`room-${index}`"
          class="bg-[var(--color-butter2)] border-2 border-[var(--color-choco)] border-solid rounded-[20px] relative cursor-pointer hover:scale-105 transition-transform"
          style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
          @click="onRoomClick(room)"
        >
          <!-- 인원수 -->
          <div class="absolute left-1/2 top-[24.5px] transform -translate-x-1/2 -translate-y-1/2">
            <p class="text-[var(--color-syrup)] text-[28px] font-['PfStardust30S'] font-normal leading-none text-center">
              {{ room.currentMembers }}/{{ room.maxMembers }} (인원수)
            </p>
          </div>
          
          <!-- 방제목 -->
          <div class="absolute left-1/2 top-[87px] transform -translate-x-1/2 -translate-y-1/2">
            <p class="text-[var(--color-choco)] text-[32px] font-['Xcu'] font-medium leading-none text-center">
              {{ room.title || '방제목' }}
            </p>
          </div>
          
          <!-- 입장 버튼 -->
          <div class="absolute right-[20px] top-[25px] transform -translate-y-1/2">
            <span class="text-[var(--color-choco)] text-[28px] font-['Xcu'] font-medium leading-none">
              입장
            </span>
          </div>
        </div>
        
        <!-- 빈 슬롯들을 채우기 위한 더미 요소 -->
        <div 
          v-for="index in Math.max(0, 6 - displayedRooms.length)" 
          :key="`empty-${index}`"
          class="bg-[var(--color-butter2)] border-2 border-[var(--color-choco)] border-solid rounded-[20px] relative opacity-30"
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
    
    <!-- 페이지네이션 -->
    <div class="h-[28px] mt-[15px] flex items-center justify-center relative">
      <!-- 페이지 네비게이션 버튼들 -->
      <div class="flex items-center gap-[24px]">
        <!-- 맨 앞으로 -->
        <button @click="goToFirstPage" class="w-[28px] h-[28px] relative">
          <img 
            src="http://localhost:3845/assets/e7094fb70c25389bc2a7e5dd6067e06911a26b8b.svg" 
            alt="맨 앞으로"
            class="w-full h-full"
          />
        </button>
        
        <!-- 이전 페이지 -->
        <button @click="goToPrevPage" class="w-[28px] h-[28px] relative">
          <img 
            src="http://localhost:3845/assets/e7094fb70c25389bc2a7e5dd6067e06911a26b8b.svg" 
            alt="이전 페이지"
            class="w-full h-full"
          />
        </button>
        
        <!-- 페이지 번호들 -->
        <div class="flex items-center gap-[12px]">
          <button 
            v-for="pageNum in visiblePages" 
            :key="pageNum"
            @click="goToPage(pageNum)"
            class="w-[28px] h-[28px] flex items-center justify-center"
          >
            <!-- <span 
              :class="[
                'text-[20px] font-[\"PfStardust30S\"] font-normal leading-none tracking-[-0.8px] text-center',
                pageNum === currentPage ? 'text-[var(--color-butter)] text-shadow-[1px_1px_0px_var(--color-butter2)]' : 'text-[var(--color-choco)] text-shadow-[1px_1px_0px_var(--color-butter2)]'
              ]"
            >
              {{ pageNum }}
            </span> -->
          </button>
        </div>
        
        <!-- 다음 페이지 -->
        <button @click="goToNextPage" class="w-[28px] h-[28px] relative">
          <img 
            src="http://localhost:3845/assets/e7094fb70c25389bc2a7e5dd6067e06911a26b8b.svg" 
            alt="다음 페이지"
            class="w-full h-full transform rotate-180"
          />
        </button>
        
        <!-- 맨 뒤로 -->
        <button @click="goToLastPage" class="w-[28px] h-[28px] relative">
          <img 
            src="http://localhost:3845/assets/e7094fb70c25389bc2a7e5dd6067e06911a26b8b.svg" 
            alt="맨 뒤로"
            class="w-full h-full transform rotate-180"
          />
        </button>
      </div>
      
      <!-- 페이지 정보 -->
      <div class="absolute right-0 top-[6px]">
        <span class="text-[var(--color-butter2)] text-[20px] font-['PfStardust30S'] font-normal leading-none tracking-[-0.8px]">
          {{ String(currentPage).padStart(2, '0') }}-{{ String(Math.min(currentPage * 6, totalRooms)).padStart(2, '0') }} / {{ totalRooms }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

interface Room {
  id: number;
  title: string;
  currentMembers: number;
  maxMembers: number;
  createdAt: string;
}

interface RoomListProps {
  rooms?: Room[];
}

// Props 정의
const props = withDefaults(defineProps<RoomListProps>(), {
  rooms: () => []
});

// Emits 정의
const emit = defineEmits<{
  roomClick: [room: Room];
  filterChange: [filter: string];
  sortChange: [sortBy: string];
  search: [query: string];
}>();

// Reactive state
const currentFilter = ref<string>('all');
const sortBy = ref<string>('popular');
const searchQuery = ref<string>('');
const currentPage = ref<number>(1);

// Computed
const totalRooms = computed(() => props.rooms.length);
const totalPages = computed(() => Math.ceil(totalRooms.value / 6));

const filteredRooms = computed(() => {
  let filtered = [...props.rooms];
  
  // 필터링 로직
  if (currentFilter.value === 'myRooms') {
    // 추후 사용자 방 필터링 로직 구현
  }
  
  // 검색 필터링
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(room => 
      room.title.toLowerCase().includes(query)
    );
  }
  
  // 정렬 로직
  if (sortBy.value === 'popular') {
    filtered.sort((a, b) => b.currentMembers - a.currentMembers);
  } else if (sortBy.value === 'latest') {
    filtered.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }
  
  return filtered;
});

const displayedRooms = computed(() => {
  const start = (currentPage.value - 1) * 6;
  const end = start + 6;
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
  emit('filterChange', filter);
};

const setSortBy = (sort: string) => {
  sortBy.value = sort;
  currentPage.value = 1;
  emit('sortChange', sort);
};

const onSearch = () => {
  currentPage.value = 1;
  emit('search', searchQuery.value);
};

const onRoomClick = (room: Room) => {
  emit('roomClick', room);
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

// Watch for rooms prop changes
watch(() => props.rooms, () => {
  currentPage.value = 1;
});
</script>