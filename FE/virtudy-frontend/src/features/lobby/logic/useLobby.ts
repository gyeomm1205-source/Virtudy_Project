import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { lobbyAPI } from '../api/lobbyAPI';
import { getMyProfile } from '@/features/mypage/api/mypageApi';
import type { RoomData, CreateRoomReq, UpdateRoomReq, ApiErrorResponse } from '../types/lobby.types';
import { storeToRefs } from 'pinia';
import { useAuthStore } from '@/stores/authStore';
import { useStudyStore } from '@/stores/studyStore';
import { useUiStore } from '@/stores/uiStore'; 

// 사용자 정보 스토어

export function useLobby() {
  const router = useRouter();

  const authStore = useAuthStore();
  const studyStore = useStudyStore();
  const uiStore = useUiStore(); 
  const { userId } = storeToRefs(authStore);

  // 상태 (State)
  const publicRooms = ref<RoomData[]>([]); // 전체 방 목록
  const myRooms = ref<RoomData[]>([]);     // 내 방 목록
  const isLoading = ref(false);

  // 데이터 불러오기 (전체 & 내 방 동시 조회)
  const fetchAllRooms = async () => {

    if (!userId.value) return;

    isLoading.value = true;
    try {
      // 병렬 요청으로 속도 최적화
      const [pubRes, myRes, profileRes] = await Promise.all([
        lobbyAPI.getPublicRooms(),
        lobbyAPI.getMyRooms(userId.value),
        getMyProfile().catch(() => null)
      ]);
      publicRooms.value = pubRes.data;
      const favoriteTitle = profileRes?.favoriteRoomTitle;
      myRooms.value = myRes.data.map(room => ({
        ...room,
        favorite: Boolean(favoriteTitle && room.title === favoriteTitle)
      }));
    } catch (error) {
      console.error('방 목록 로딩 실패', error);
    } finally {
      isLoading.value = false;
    }
  };

  // 방 생성
  const createRoom = async (reqData: CreateRoomReq) => {
    if (!userId.value) {
      await uiStore.openAlert('로그인이 필요한 서비스입니다.', '알림');
      return false;
    }
    try {
      await lobbyAPI.createRoom(userId.value, reqData);
      await fetchAllRooms(); // 목록 갱신
      
      await uiStore.openAlert('스터디방이 생성되었습니다!', '성공');
      
      return true; // 성공 시 true 반환 (모달 닫기용)
    } catch (e: any) {
      await handleApiError(e); 
      return false;
    }
  };

  // 방 수정
  const updateRoom = async (roomId: string, reqData: UpdateRoomReq) => {
    if (!userId.value) {
      await uiStore.openAlert('로그인이 필요한 서비스입니다.', '알림');
      return false;
    }
    try {
      await lobbyAPI.updateRoom(userId.value, roomId, reqData);
      await fetchAllRooms(); // 목록 갱신 (수정된 제목/설명 반영)
      
      await uiStore.openAlert('스터디방 정보가 수정되었습니다.', '성공');
      
      return true; // 성공
    } catch (e: any) {
      await handleApiError(e);
      return false;
    }
  };


  // 방 삭제
  const deleteRoom = async (roomId: string) => {
    // confirm -> custom modal (boolean 반환 이용)
    // 확인 누르면 true, 닫기 누르면 false
    const confirmed = await uiStore.openAlert('정말 이 스터디방을 삭제하시겠습니까?', '삭제 확인');
    if (!confirmed) return;
    
    if (!userId.value) {
      await uiStore.openAlert('로그인이 필요한 서비스입니다.', '알림');
      return;
    }

    try {
      await lobbyAPI.deleteRoom(userId.value, roomId);
      await uiStore.openAlert('삭제되었습니다.', '알림');
      await fetchAllRooms(); // 목록 갱신
    } catch (e: any) {
      await handleApiError(e);
    }
  };

  // 방 입장 (토큰 발급 -> 페이지 이동)
  const joinRoom = async (roomId: string, password?: string): Promise<void> => {
    if (!userId.value) {
      await uiStore.openAlert('로그인이 필요한 서비스입니다.', '알림');
      return;
    }
    try {
      // 입장 API 호출
      const sessionData = await lobbyAPI.enterRoom(userId.value, roomId, password);
      // 테스트를 위해 콘솔에 토큰 출력
      console.log('✅ 입장 성공! 토큰:', sessionData.liveKitToken);
      // 토큰을 sessionStorage에 저장 (탭 내 새로고침 대응)
      studyStore.setToken(sessionData.liveKitToken, roomId);
      // 토큰 없이 스터디 룸 페이지로 이동
      router.push({ 
        name: 'StudyRoom', 
        params: { roomId },
        query: { from: 'lobby' }
      });
      return true;
    } catch (e: any) {
      await handleApiError(e);
    }
  };

  // ✅ 최애방 설정 토글
  const toggleFavoriteRoom = async (roomId: string) => {
    if (!userId.value) {
      await uiStore.openAlert('로그인이 필요한 서비스입니다.', '알림');
      return;
    }

    // 1. 현재 내 방 목록에서 해당 방 찾기
    const targetRoom = myRooms.value.find(r => r.roomId === roomId);
    if (!targetRoom) return;

    // 2. 이미 최애방이면 유지 (해제 기능 없음)
    if (targetRoom.favorite) return;

    // 3. 최애방 설정 API 호출 (다른 방으로 교체)
    try {
      await lobbyAPI.toggleFavorite(userId.value, roomId);
      // 상태 업데이트: 다른 방의 최애방 해제 후 선택 방만 설정
      myRooms.value.forEach(room => {
        room.favorite = room.roomId === roomId;
      });
      // await fetchAllRooms();
    } catch (e: any) {
      await handleApiError(e);
    }
  };

  // 공통 에러 핸들러 (async로 변경)
  const handleApiError = async (error: any) => {
    const errRes = error.response?.data as ApiErrorResponse;
    if (errRes) {
      await uiStore.openAlert(`[${errRes.code}] ${errRes.message}`, '오류');
    } else {
      await uiStore.openAlert('알 수 없는 오류가 발생했습니다.', '오류');
    }
  };

  return {
    publicRooms,
    myRooms,
    isLoading,
    fetchAllRooms,
    createRoom,
    updateRoom,
    joinRoom,
    deleteRoom,
    toggleFavoriteRoom
  };
}