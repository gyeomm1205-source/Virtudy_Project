<template>
  <div class="bg-[var(--color-syrup)] relative w-full h-screen">
    <!-- Navigation -->
    <GlobalNavBar />
    
    <!-- Footer -->
    <GlobalFooter />
    
    <!-- 뒤로가기 버튼 -->
    <div class="absolute left-[76px] top-[119px] w-[54px] h-[54px] cursor-pointer" @click="goBack">
      <img 
        src="http://localhost:3845/assets/8b3a74cfd86bc6957d7b678cb811aa5429eb437e.svg" 
        alt="뒤로가기"
        class="w-full h-full object-contain"
      />
    </div>
    
    <!-- 방목록 제목 -->
    <div class="absolute left-[58px] top-[324px] transform -translate-y-1/2">
      <h1 class="text-[var(--color-pancake)] text-[156px] font-['Ram'] font-medium leading-none tracking-[-18.72px]">
        방목록
      </h1>
    </div>
    
    <!-- 왼쪽 - 프로필과 메뉴 -->
    <div class="absolute left-[calc(8.33%+107px)] top-[361px] w-[255px] h-[406px]">
      <LobbyProfile 
        :user-profile-image="userProfileImage"
        @random-match="handleRandomMatch"
        @create-room="handleCreateRoom"
      />
    </div>
    
    <!-- 오른쪽 - 방 목록 -->
    <div class="absolute left-[calc(33.33%+38px)] top-[95px] w-[691px] h-[686px]">
      <RoomList 
        :rooms="rooms"
        @room-click="handleRoomClick"
        @filter-change="handleFilterChange"
        @sort-change="handleSortChange"
        @search="handleSearch"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import LobbyProfile from '@/shared/ui/LobbyProfile.vue';
import RoomList from '@/shared/ui/RoomList.vue';

interface Room {
  id: number;
  title: string;
  currentMembers: number;
  maxMembers: number;
  createdAt: string;
}

const router = useRouter();

// Reactive state
const userProfileImage = ref<string | null>(null);
const rooms = ref<Room[]>([]);

// 더미 데이터 (추후 API 연동으로 교체)
const initializeDummyRooms = () => {
  rooms.value = [
    {
      id: 1,
      title: "알고리즘 스터디",
      currentMembers: 0,
      maxMembers: 6,
      createdAt: "2024-01-26T10:00:00Z"
    },
    {
      id: 2,
      title: "코딩테스트 준비",
      currentMembers: 3,
      maxMembers: 6,
      createdAt: "2024-01-26T11:00:00Z"
    },
    {
      id: 3,
      title: "프론트엔드 스터디",
      currentMembers: 2,
      maxMembers: 6,
      createdAt: "2024-01-26T12:00:00Z"
    },
    {
      id: 4,
      title: "백엔드 개발",
      currentMembers: 1,
      maxMembers: 6,
      createdAt: "2024-01-26T13:00:00Z"
    }
  ];
};

// Event handlers
const handleRandomMatch = () => {
  console.log('랜덤매칭 요청');
  // 랜덤매칭 로직 구현
};

const handleCreateRoom = () => {
  console.log('방만들기 요청');
  // 방만들기 페이지로 이동하거나 모달 열기
};

const handleRoomClick = (room: Room) => {
  console.log('방 클릭:', room);
  // 방 입장 로직 구현
};

const handleFilterChange = (filter: string) => {
  console.log('필터 변경:', filter);
  // 필터 변경 로직 구현
};

const handleSortChange = (sortBy: string) => {
  console.log('정렬 변경:', sortBy);
  // 정렬 변경 로직 구현
};

const handleSearch = (query: string) => {
  console.log('검색:', query);
  // 검색 로직 구현
};

const goBack = () => {
  router.back();
};

// Lifecycle
onMounted(() => {
  initializeDummyRooms();
});
</script>