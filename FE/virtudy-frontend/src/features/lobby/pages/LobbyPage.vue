<template>
  <div class="bg-[var(--color-syrup)] w-full min-h-screen flex flex-col relative">
    
    <div class="flex-none z-50">
      <GlobalNavBar />
    </div>
    
    <main class="flex-1 w-full relative min-h-[850px]">
      
      <div class="absolute left-[76px] top-[119px] w-[54px] h-[54px] cursor-pointer hover:scale-110 transition-transform" @click="goBack">
        <svg viewBox="0 0 54 54" class="w-full h-full" fill="var(--color-choco)">
          <path d="M40 22H18.8L29.4 11.4L27 9L13 23L27 37L29.4 34.6L18.8 24H40V22Z"/>
        </svg>
      </div>
      
      <div class="absolute left-[58px] top-[324px] transform -translate-y-1/2">
        <h1 class="text-[var(--color-pancake)] text-[156px] font-['Ram'] font-medium leading-none tracking-[-18.72px]">
          방목록
        </h1>
      </div>
      
      <div class="absolute left-[calc(8.33%+107px)] top-[361px] w-[255px] h-[406px]">
        <div class="absolute left-[54px] top-[83px] w-[146px] h-[146px] rounded-full overflow-hidden border-4 border-[var(--color-choco)]">
          <img 
            src="http://localhost:3845/assets/ae7ca0939b29738c16aee5cf86953e893d60c594.svg"
            alt="프로필 사진"
            class="w-full h-full object-cover"
          />
        </div>
        
        <div class="absolute top-[260px] w-full flex flex-col gap-[10px]">
          <button class="butter-btn bg-[var(--color-butter)] w-full" @click="randomMatch">
            <span class="text-[var(--color-choco)] text-[28px] font-['Xcu'] font-medium leading-none">
              랜덤매칭
            </span>
          </button>
          <button class="butter-btn bg-[var(--color-butter2)] w-full" @click="createRoom">
            <span class="text-[var(--color-choco)] text-[28px] font-['Xcu'] font-medium leading-none">
              방만들기
            </span>
          </button>
        </div>
      </div>
      
      <div class="absolute left-[calc(33.33%+38px)] top-[95px] w-[691px] h-[686px]">
        <RoomList 
          :rooms="allRooms"
          @roomClick="onRoomClick"
          @filterChange="setFilter"
          @sortChange="setSortBy"
          @search="onSearchInput"
        />
      </div>

    </main>
    
    <div class="flex-none">
      <GlobalFooter />
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import RoomList from '@/shared/ui/RoomList.vue';

const MOCK_MEMBER_ID = 'test-member-001';

interface Room {
  roomId: string;
  title: string;
  type?: string;
  currentMembers?: number;
  maxMembers?: number;
  createdAt?: string;
}

const emit = defineEmits<{
  roomClick: [room: Room];
}>();

const router = useRouter();

// State
const allRooms = ref<Room[]>([]);
const currentFilter = ref<string>('all');
const sortBy = ref<string>('popular');
const searchQuery = ref<string>('');
const currentPage = ref<number>(1);
const ITEMS_PER_PAGE = 6;

// Methods
const goBack = () => router.back();

const fetchRooms = async () => {
  allRooms.value = [];
  try {
    let response;
    if (currentFilter.value === 'all') {
      response = await axios.get('/api/study-rooms/');
    } else {
      response = await axios.get('/api/study-rooms/my', {
        headers: { 'X-MEMBER-ID': MOCK_MEMBER_ID }
      });
    }
    if (response && response.data) {
      allRooms.value = response.data;
    }
  } catch (error) {
    console.error('방 목록 조회 실패:', error);
    allRooms.value = [];
  }
};

const filteredRooms = computed(() => {
  let filtered = [...allRooms.value];
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(room => 
      room.title?.toLowerCase().includes(query)
    );
  }
  if (sortBy.value === 'popular') {
    filtered.sort((a, b) => (b.currentMembers || 0) - (a.currentMembers || 0));
  } else if (sortBy.value === 'latest') {
    filtered.sort((a, b) => {
      const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
      const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
      return dateB - dateA;
    });
  }
  return filtered;
});

const displayedRooms = computed(() => {
  const start = (currentPage.value - 1) * ITEMS_PER_PAGE;
  const end = start + ITEMS_PER_PAGE;
  return filteredRooms.value.slice(start, end);
});

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredRooms.value.length / ITEMS_PER_PAGE));
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

const setFilter = (filter: string) => {
  if (currentFilter.value === filter) return;
  currentFilter.value = filter;
  currentPage.value = 1;
  searchQuery.value = '';
  fetchRooms();
};

const setSortBy = (sort: string) => {
  sortBy.value = sort;
  currentPage.value = 1;
};

const onSearchInput = (query: string) => {
  searchQuery.value = query;
  currentPage.value = 1;
};

const onRoomClick = (room: Room) => {
  emit('roomClick', room);
  router.push(`/study-room/${room.roomId}`);
};

const randomMatch = () => {
  console.log('랜덤매칭 시작');
};

const createRoom = () => {
  router.push('/create-room');
};

onMounted(() => {
  fetchRooms();
});
</script>