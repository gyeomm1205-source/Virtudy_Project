// 방 목록 조회, 방 생성 API (CRUD)

import axios from 'axios';
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
    return axios.get<RoomData[]>('/study-rooms');
  },

  /**
   * 2. 내 스터디방 목록 조회
   * GET /api/study-rooms/my
   * - 내가 속한(방장이거나 참여중인) 방 최신순 10개
   */
  getMyRooms: async (memberId: string) => {
    return axios.get<RoomData[]>('/study-rooms/my', {
      headers: { 'X-MEMBER-ID': memberId }
    });
  },

  /**
   * 3. 스터디방 생성
   * POST /api/study-rooms
   * - 성공 시 생성된 RoomData 반환
   */
  createRoom: async (memberId: string, data: CreateRoomReq) => {
    return axios.post<RoomData>('/study-rooms', data, {
      headers: { 'X-MEMBER-ID': memberId }
    });
  },

  /**
   * 4. 코드로 스터디방 조회 (상세 조회)
   * GET /api/study-rooms/{roomId}
   */
  getRoomDetail: async (roomId: string) => {
    return axios.get<RoomData>(`/study-rooms/${roomId}`);
  },

  /**
   * 5. 스터디방 정보 수정
   * PATCH /api/study-rooms/{roomId}
   */
  updateRoom: async (memberId: string, roomId: string, data: UpdateRoomReq) => {
    return axios.patch(`/study-rooms/${roomId}`, data, {
      headers: { 'X-MEMBER-ID': memberId }
    });
  },

  /**
   * 6. 스터디방 삭제 (종료)
   * DELETE /api/study-rooms/{roomId}
   */
  deleteRoom: async (memberId: string, roomId: string) => {
    return axios.delete(`/study-rooms/${roomId}`, {
      headers: { 'X-MEMBER-ID': memberId }
    });
  },

  /**
   * 7. 최애 스터디방 설정
   * PATCH /api/study-rooms/favorite/{roomId}
   */
  toggleFavorite: async (memberId: string, roomId: string) => {
    return axios.patch(`/study-rooms/favorite/${roomId}`, {}, {
      headers: { 'X-MEMBER-ID': memberId }
    });
  },

  // ==================================================
  // Session 관련 (입장)
  // ==================================================

  /**
   * 8. 특정 스터디방 입장
   * POST /api/sessions/enter/{roomId}
   */
  enterRoom: async (memberId: string, roomId: string, password?: string) => {
    // Body에 비밀번호를 담아서 보냄 (EnterRoomReq 구조 맞춤)
    const body: EnterRoomReq = { password };
    
    return axios.post<EnterSessionRes>(
      `/sessions/enter/${roomId}`, 
      body, 
      { headers: { 'X-MEMBER-ID': memberId } }
    );
  },

  /**
   * 9. 랜덤 스터디방 입장
   * POST /api/sessions/enter/random
   */
  enterRandomRoom: async (memberId: string) => {
    return axios.post<EnterSessionRes>(
      '/sessions/enter/random', 
      {}, 
      { headers: { 'X-MEMBER-ID': memberId } }
    );
  }
};