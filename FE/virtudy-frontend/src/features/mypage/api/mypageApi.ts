import api from '@/shared/api/axios.config'; // 설정된 axios 인스턴스
import type { UserProfileResponse, ProfileUpdateRequest } from '../types/mypage.types';

// 내 프로필 조회
export const getMyProfile = async (): Promise<UserProfileResponse> => {
  const { data } = await api.get<UserProfileResponse>('/members/profile');
  return data;
};

// 내 프로필 수정
export const updateMyProfile = async (payload: ProfileUpdateRequest): Promise<UserProfileResponse> => {
  const { data } = await api.patch<UserProfileResponse>('/members/profile', payload);
  return data;
};