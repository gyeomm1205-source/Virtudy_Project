import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { lobbyAPI } from '../api/lobbyAPI';
import type { RoomData, CreateRoomReq, ApiErrorResponse } from '../types/lobby.types';
import { storeToRefs } from 'pinia';
import { useAuthStore } from '@/stores/authStore';

// 사용자 정보 스토어

export function useLobby() {
  const router = useRouter();

  const authStore = useAuthStore();
  const { userId } = storeToRefs(authStore);
  
  // 상태 (State)
  const publicRooms = ref<RoomData[]>([]); // 전체 방 목록
  const myRooms = ref<RoomData[]>([]);     // 내 방 목록
  const isLoading = ref(false);
  
  // 내 방 ID 목록 (삭제 버튼 노출용 Set)
  const myRoomIds = computed(() => new Set(myRooms.value.map(r => r.roomId)));

  // 데이터 불러오기 (전체 & 내 방 동시 조회)
  const fetchAllRooms = async () => {

    if (!userId.value) {
    alert('로그인이 필요한 서비스입니다.');
    return; 
    }

    isLoading.value = true;
    try {
      // 병렬 요청으로 속도 최적화
      const [pubRes, myRes] = await Promise.all([
        lobbyAPI.getPublicRooms(),
        lobbyAPI.getMyRooms(userId.value)
      ]);
      publicRooms.value = pubRes.data;
      myRooms.value = myRes.data;
    } catch (error) {
      console.error('방 목록 로딩 실패', error);
    } finally {
      isLoading.value = false;
    }
  };

  // 방장이 맞는지 확인하는 헬퍼 함수
  const isMyRoom = (roomId: string) => {
    return myRoomIds.value.has(roomId);
  };

  // 방 생성
  const createRoom = async (reqData: CreateRoomReq) => {
    if (!userId.value) {
      alert('로그인이 필요한 서비스입니다.');
      return false;
    }
    try {
      await lobbyAPI.createRoom(userId.value, reqData);
      await fetchAllRooms(); // 목록 갱신
      alert('스터디방이 생성되었습니다!');
      return true; // 성공 시 true 반환 (모달 닫기용)
    } catch (e: any) {
      handleApiError(e);
      return false;
    }
  };

  // 방 입장 (토큰 발급 -> 페이지 이동)
  const joinRoom = async (roomId: string, password?: string) => {
    if (!userId.value) {
      alert('로그인이 필요한 서비스입니다.');
      return;
    }
    try {
      // 입장 API 호출
      const { data } = await lobbyAPI.enterRoom(userId.value, roomId, password);
      
      // 테스트를 위해 콘솔에 토큰 출력
      console.log('✅ 입장 성공! 토큰:', data.liveKitToken);
      
      // 토큰을 가지고 스터디 룸 페이지로 이동
      router.push({ name: 'StudyRoom', params: { roomId }, query: { token: data.liveKitToken } });
      
    } catch (e: any) {
      handleApiError(e);
    }
  };

  // 방 삭제
  const deleteRoom = async (roomId: string) => {
    if (!confirm('정말 이 스터디방을 삭제하시겠습니까?')) return;
    if (!userId.value) {
    alert('로그인이 필요한 서비스입니다.');
    return; 
    }
    try {
      await lobbyAPI.deleteRoom(userId.value, roomId);
      alert('삭제되었습니다.');
      await fetchAllRooms(); // 목록 갱신
    } catch (e: any) {
      handleApiError(e);
    }
  };

  // 공통 에러 핸들러
  const handleApiError = (error: any) => {
    const errRes = error.response?.data as ApiErrorResponse;
    if (errRes) {
      alert(`[${errRes.code}] ${errRes.message}`);
    } else {
      alert('알 수 없는 오류가 발생했습니다.');
    }
  };

  return {
    publicRooms,
    myRooms,
    isLoading,
    isMyRoom,
    fetchAllRooms,
    createRoom,
    joinRoom,
    deleteRoom
  };
}