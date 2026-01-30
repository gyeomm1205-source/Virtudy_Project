// 방 목록 조회, 방 생성 API (CRUD)

// import axios from 'axios';
import api from '@/shared/api/axios.config'; // ✅ 설정된 axios instance 사용
import type { 
  RoomData, 
  CreateRoomReq, 
  UpdateRoomReq, 
  EnterRoomReq,
  EnterSessionRes 
} from '../types/lobby.types';

export const lobbyAPI = {
  /**
   * 1. 전체 스터디방 목록 조회 (공개방)
   * GET /api/study-rooms
   */
  getPublicRooms: async () => {
    return api.get<RoomData[]>('/study-rooms');
  },

  /**
   * 2. 내 스터디방 목록 조회
   * GET /api/study-rooms/my
   * - 내가 속한(방장이거나 참여중인) 방 최신순 10개
   */
  getMyRooms: async (userId: string) => {
    return api.get<RoomData[]>('/study-rooms/my', {
      headers: { 'X-MEMBER-ID': userId }
    });
  },

  /**
   * 3. 스터디방 생성
   * POST /api/study-rooms
   * - 성공 시 생성된 RoomData 반환
   */
  createRoom: async (userId: string, data: CreateRoomReq) => {
    return api.post<RoomData>('/study-rooms', data, {
      headers: { 'X-MEMBER-ID': userId }
    });
  },

  /**
   * 4. 코드로 스터디방 조회 (상세 조회)
   * GET /api/study-rooms/{roomId}
   */
  getRoomDetail: async (roomId: string) => {
    return api.get<RoomData>(`/study-rooms/${roomId}`);
  },

  /**
   * 5. 스터디방 정보 수정
   * PATCH /api/study-rooms/{roomId}
   */
  updateRoom: async (userId: string, roomId: string, data: UpdateRoomReq) => {
    return api.patch(`/study-rooms/${roomId}`, data, {
      headers: { 'X-MEMBER-ID': userId }
    });
  },

  /**
   * 6. 스터디방 삭제 (종료)
   * DELETE /api/study-rooms/{roomId}
   */
  deleteRoom: async (userId: string, roomId: string) => {
    return api.delete(`/study-rooms/${roomId}`, {
      headers: { 'X-MEMBER-ID': userId }
    });
  },

  /**
   * 7. 최애 스터디방 설정
   * PATCH /api/study-rooms/favorite/{roomId}
   */
  toggleFavorite: async (userId: string, roomId: string) => {
    return api.patch(`/study-rooms/favorite/${roomId}`, {}, {
      headers: { 'X-MEMBER-ID': userId }
    });
  },

  // ==================================================
  // Session 관련 (입장)
  // ==================================================

  /**
   * 8. 특정 스터디방 입장
   * POST /api/sessions/enter/{roomId}
   */
  enterRoom: async (userId: string, roomId: string, password?: string) => {
    const body: EnterRoomReq = { password };
    
    // 1. Axios 응답을 변수에 받음
    const response = await api.post<EnterSessionRes>(
      `/sessions/enter/${roomId}`, 
      body, 
      { headers: { 'X-MEMBER-ID': userId } }
    );

    // 2. data만 꺼내서 + memberId 매핑까지 해서 반환
    return {
      userId: response.data.memberId,
      nickName: response.data.nickName,
      avatar: response.data.avatar,
      liveKitToken: response.data.liveKitToken
    };
  },
  /**
   * 9. 랜덤 스터디방 입장
   * POST /api/sessions/enter/random
   */
  enterRandomRoom: async (userId: string) => {
    const response = await api.post<EnterSessionRes>(
      '/sessions/enter/random',
      {},
      { headers: { 'X-MEMBER-ID': userId } }
    );

    return {
      userId: response.data.memberId,
      nickName: response.data.nickName,
      avatar: response.data.avatar,
      liveKitToken: response.data.liveKitToken
    };
  }
};