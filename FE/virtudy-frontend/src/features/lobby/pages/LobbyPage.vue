<template>
  <div class="bg-[var(--color-syrup)] w-full min-h-screen flex flex-col relative">
    
    <div class="flex-none z-50">
      <GlobalNavBar />
    </div>
    
    <main class="flex-1 w-full relative min-h-[850px] lobby-main">
      
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
      
      <div class="absolute left-[calc(8.33%+107px)] top-[361px] w-[255px] h-[406px] lobby-actions">
        <div class="absolute left-[100px] top-[140px] w-[200px] h-[200px]">
          <CharacterAvatar
            v-if="hasAvatarConfig"
            :config="authStore.userInfo!.avatar!"
            class="w-full h-full"
          />
          <img
            v-else-if="authStore.userInfo?.avatarImageUrl"
            :src="authStore.userInfo.avatarImageUrl"
            alt="프로필"
            class="w-full h-full object-cover"
          />
        </div>
        
        <div class="absolute top-[260px] w-full flex flex-col gap-[10px] lobby-action-buttons">
          <button class="butter-btn bg-[var(--color-butter)] w-full" @click="handleRandomMatch">
            <span class="text-[var(--color-choco)] text-[28px] font-['Xcu'] font-medium leading-none">
              랜덤매칭
            </span>
          </button>
          
          <button class="butter-btn bg-[var(--color-butter2)] w-full" @click="openCreateModal">
            <span class="text-[var(--color-choco)] text-[28px] font-['Xcu'] font-medium leading-none">
              방만들기
            </span>
          </button>
        </div>
      </div>
      
      <div class="absolute left-[calc(33.33%+38px)] top-[95px] w-[691px] h-[686px] lobby-roomlist">
        <RoomList 
          :rooms="displayedRooms"
          :isMyRoomTab="currentFilter === 'myRooms'"  @roomClick="handleRoomClick"
          @filterChange="setFilter"
          @search="onSearchInput"
          @edit="handleEditRoom"      
          @delete="handleDeleteRoom"
          @toggleFavorite="handleToggleFavorite" />
      </div>

      <div
        v-if="showFavoriteToast"
        class="absolute left-1/2 top-[110px] -translate-x-1/2 z-40 border-2 bg-[var(--color-butter2)]/80 rounded-[20px] px-[18px] py-[10px]"
        style="border-color: color-mix(in srgb, var(--color-choco) 80%, transparent); box-shadow: 4px 4px 0px 0px color-mix(in srgb, var(--color-choco) 80%, transparent);"
      >
        <span class="text-[var(--color-choco)] text-[18px] font-['PfStardust30S'] font-normal leading-none">
          {{ favoriteToastMessage }}
        </span>
      </div>

    </main>
    
    <div class="flex-none">
      <GlobalFooter />
    </div>

  <CreateRoomModal 
    v-if="showModal" 
    :initialData="selectedRoom" 
    @close="showModal = false"
    @success="fetchAllRooms" 
  />

  <MatchingModal
    v-if="isEntering"
    title-text="입장 중..."
    subtitle-text="잠시만 기다려주세요..."
    @close="isEntering = false"
  />

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';

// ✅ FSD 모듈 import
import { useAuthStore } from '@/stores/authStore';
import { useLobby } from '@/features/lobby/logic/useLobby';
import { lobbyAPI } from '@/features/lobby/api/lobbyAPI'; // 랜덤매칭용
import type { RoomData } from '@/features/lobby/types/lobby.types'; // 방 데이터 타입
// ✅ UI 컴포넌트 import
import GlobalNavBar from '@/shared/ui/GlobalNavBar.vue';
import GlobalFooter from '@/shared/ui/GlobalFooter.vue';
import RoomList from '@/shared/ui/RoomList.vue';
import CreateRoomModal from '../ui/CreateRoomModal.vue'; // 새로 만든 모달
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import MatchingModal from '@/shared/ui/MatchingModal.vue';

import { maxMembers } from '@/shared/config/constants'; // 상수 import

const router = useRouter();

// 1. Store & Hook 연결
const authStore = useAuthStore();
const { userId } = storeToRefs(authStore);

const hasAvatarConfig = computed(() => {
  if (!authStore.userInfo?.avatar) return false;
  return Object.values(authStore.userInfo.avatar).some((value) => Boolean(value));
});

const { 
  publicRooms, 
  myRooms, 
  // isLoading, // 로딩바 필요하면 사용
  fetchAllRooms, 
  joinRoom,
  deleteRoom,
  updateRoom,
  toggleFavoriteRoom
} = useLobby();

// UI 상태 관리
const showModal = ref(false); // 모달 표시 여부
const selectedRoom = ref<RoomData | null>(null);
const currentFilter = ref<string>('all'); // 'all' | 'my'
const searchQuery = ref<string>('');
const showFavoriteToast = ref(false);
const favoriteToastMessage = ref('');
let favoriteToastTimer: ReturnType<typeof setTimeout> | null = null;
const isEntering = ref(false);

// Methods
const goBack = () => router.back();

// 방 만들기 버튼 클릭 (모달 열기)
const openCreateModal = () => {
  if (!userId.value) {
    alert('로그인이 필요한 서비스입니다.');
    return;
  }
  selectedRoom.value = null; // 생성 모드이므로 데이터 비우기
  showModal.value = true;
};

// 방 수정 버튼 클릭 
const handleEditRoom = async (room: any) => {
  try {
    const { data } = await lobbyAPI.getRoomDetail(room.roomId);
    selectedRoom.value = data;
    showModal.value = true;
  } catch (error) {
    console.error('방 상세 조회 실패:', error);
    alert('방 정보를 불러오지 못했습니다.');
  }
};

// ✅ 방 삭제 버튼 클릭
const handleDeleteRoom = async (roomId: string) => {
    // useLobby에서 가져온 deleteRoom 함수 사용
    await deleteRoom(roomId);
};

// 랜덤 매칭
const handleRandomMatch = async () => {
  if (!userId.value) {
    alert('로그인이 필요합니다.');
    return;
  }
  try {
    const  data  = await lobbyAPI.enterRandomRoom(userId.value);
    // 입장 성공 -> 스터디룸으로 이동 (roomId를 사용)
    router.push(`/study/${data.roomId}?token=${data.liveKitToken}`);
  } catch (e) {
    console.error(e);
    alert('입장 가능한 방이 없습니다.');
  }
};

// 방 클릭 (입장 로직)
const handleRoomClick = async (room: any) => {
  if (isEntering.value) return;
  isEntering.value = true;
  try {
    // RoomList에서 넘어오는 room 객체의 ID 사용
    const success = await joinRoom(room.roomId);
    if (!success) {
      isEntering.value = false;
    }
  } catch {
    isEntering.value = false;
  }
};



// 탭 변경 (전체 <-> 내 스터디)
const setFilter = (filter: string) => {
  currentFilter.value = filter;
  searchQuery.value = '';
  fetchAllRooms(); // 탭 바꿀 때 데이터 갱신
};

const onSearchInput = (query: string) => {
  searchQuery.value = query;
};

// Computed Properties (데이터 가공)

// 현재 탭에 맞는 데이터 소스 선택
const targetSourceRooms = computed(() => {
  return currentFilter.value === 'all' ? publicRooms.value : myRooms.value;
});

// 필터링 및 정렬 로직
const filteredRooms = computed(() => {
  // 원본 데이터 복사 (type error 방지를 위해 any 캐스팅 혹은 RoomData 타입 호환 확인 필요)
  let filtered = [...targetSourceRooms.value];

  // 검색
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.trim().toLowerCase();
    filtered = filtered.filter(room => 
      room.title?.toLowerCase().includes(query) ||
      String(room.roomId ?? '').toLowerCase().includes(query)
    );
  }

  return filtered;
});

// ✅ [NEW] 하트 클릭 핸들러
const handleToggleFavorite = async (roomId: string) => {
  const targetRoom = myRooms.value.find(room => room.roomId === roomId);
  await toggleFavoriteRoom(roomId);
  if (targetRoom?.title) {
    const lastChar = targetRoom.title.trim().slice(-1);
    const code = lastChar.charCodeAt(0);
    const hasJong = code >= 0xac00 && code <= 0xd7a3 ? (code - 0xac00) % 28 !== 0 : false;
    const particle = hasJong ? '을' : '를';
    favoriteToastMessage.value = `${targetRoom.title}${particle} 최애방으로 선택했습니다!`;
    showFavoriteToast.value = true;
    if (favoriteToastTimer) {
      clearTimeout(favoriteToastTimer);
    }
    favoriteToastTimer = setTimeout(() => {
      showFavoriteToast.value = false;
      favoriteToastTimer = null;
    }, 2000);
  }
};

// 페이지네이션 및 RoomList 컴포넌트 타입 매핑
const displayedRooms = computed(() => {
  // RoomList 컴포넌트가 요구하는 Room 타입으로 변환
  return filteredRooms.value.map(room => ({
    roomId: room.roomId,
    title: room.title,
    currentMembers: room.currentUser,
    maxMembers: maxMembers,
    createdAt: new Date().toISOString(), // 현재 시간으로 설정
    owner: room.owner || false, 
    description: room.description,
    type: room.type,
    favorite: room.favorite || false // ✅ favorite 속성 추가
  }));
});



// 초기 데이터 로드
let roomsRefreshTimer: ReturnType<typeof setInterval> | null = null;

onMounted(() => {
  if (authStore.isLoggedIn && !authStore.userInfo) {
    authStore.fetchUserInfo();
  }

  if (userId.value) {
    fetchAllRooms();
  } else {
    // 비로그인 상태 처리 (필요시)
    console.warn('로그인 정보가 없습니다. (게스트 모드)');
    fetchAllRooms(); // 공개방은 볼 수 있게 할 경우
  }

  roomsRefreshTimer = setInterval(() => {
    fetchAllRooms();
  }, 5000);
});

onUnmounted(() => {
  if (roomsRefreshTimer) {
    clearInterval(roomsRefreshTimer);
    roomsRefreshTimer = null;
  }
  if (favoriteToastTimer) {
    clearTimeout(favoriteToastTimer);
    favoriteToastTimer = null;
  }
});
</script>

<style scoped>
@media (max-width: 1280px) {
  .lobby-main {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 140px;
    gap: 28px;
  }

  .lobby-actions {
    position: relative;
    left: auto;
    top: auto;
    width: min(92vw, 360px);
    height: auto;
    order: 2;
  }

  .lobby-roomlist {
    position: relative;
    left: auto;
    top: auto;
    width: min(95vw, 720px);
    height: auto;
    order: 1;
  }

  .lobby-action-buttons {
    position: static;
    margin-top: 16px;
    display: flex;
    flex-direction: row;
    gap: 12px;
  }

  .lobby-action-buttons > button {
    width: 100%;
  }
}
</style>
