import type { AvatarConfig } from '@/shared/types/common.types';

/**
 * 📝 Lobby Feature 타입 정의
 * - 기준: Virtudy API 명세서 v1.2.0 (2026.01.29 수정)
 */

// ==========================================
// 1. 공통 서브 타입 (Sub Interfaces)
// ==========================================

/** 공통 에러 응답 양식 */
export interface ApiErrorResponse {
  status: number;   // 예: 401
  error: string;    // 예: "UNAUTHORIZED"
  code: string;     // 예: "REQUEST_ERROR_003"
  message: string;  // 예: "로그인 후 이용해주세요."
  timestamp: string;
}

// ==========================================
// 2. 데이터 모델 (DTO)
// ==========================================

/**
 * 방 정보 데이터
 * - 목록 조회, 상세 조회, 생성 결과 등에서 공통으로 사용
 */
export interface RoomData {
  roomId: string;
  title: string;
  type: 'PUBLIC' | 'PRIVATE';
  currentUser: number;
  
  /** 상세 설명 (목록 조회 시엔 없을 수 있음) */
  description?: string;

  /** * ✅ [NEW] 방장(소유자) 여부 
   * - 전체 목록 조회 및 내 스터디방 목록 조회 시 포함됨
   * - true일 경우 수정/삭제 권한 부여
   */
  owner?: boolean; 
}

// ==========================================
// 3. 요청 (Request) 타입
// ==========================================

/** 스터디방 생성 요청 (POST Body) */
export interface CreateRoomReq {
  title: string;
  type: 'PUBLIC' | 'PRIVATE';
  description?: string;
  password?: string;    // PRIVATE일 때 필수
}

/** 스터디방 정보 수정 요청 (PATCH Body) */
export interface UpdateRoomReq {
  title?: string;
  description?: string;
  password?: string;
}

/** * 특정 방 입장 요청 (POST Body)
 * - 비밀번호가 있는 방일 경우 사용
 */
export interface EnterRoomReq {
  password?: string;
}

// ==========================================
// 4. 응답 (Response) 타입
// ==========================================

/**
 * 방 입장(세션) 성공 응답
 * - /api/sessions/enter/{roomId}
 * - /api/sessions/enter/random
 */
export interface EnterSessionRes {
  // ✅ 명세서에 따라 userId -> memberId로 변경됨
  memberId: string; 
  nickName: string;
  avatar: AvatarConfig;
  liveKitToken: string;
}