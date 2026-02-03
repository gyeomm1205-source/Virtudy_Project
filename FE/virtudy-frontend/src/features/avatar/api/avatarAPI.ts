import api from '@/shared/api/axios.config';
import type { AvatarConfig } from '@/shared/types/common.types';

// 환경 변수에서 AI 서버 주소 가져오기
const AI_BASE_URL = import.meta.env.VITE_AI_API_URL || '';

// AI 서버 응답 타입 정의
export interface AiAvatarResponse {
  member_id: number;
  hair_front: string;
  hair_back: string;
  hair_color: string;
  eyes: string;
  glasses: string;
  clothes: string;       // outfit에 해당 (매핑)
  clothes_color: string;
}

export const avatarAPI = {
  /**
   * AI 아바타 생성 요청
   * @param file - 웹캠으로 촬영한 이미지 파일
   * @param nickname - 사용자 닉네임
   */
  generateAvatar: async (file: File, nickname: string): Promise<AvatarConfig> => {
    // FormData 생성 (파일 전송 필수)
    const formData = new FormData();
    formData.append('file', file);
    console.log('nickname in avatarAPI:', nickname);
    formData.append('nick_name', nickname); // API 명세 키값 확인


    // API 요청 전송
    // 메인 백엔드 주소 대신 AI_BASE_URL 사용
    // Content-Type은 axios가 FormData를 보고 알아서 설정
    const response = await api.post<AiAvatarResponse>(
      `${AI_BASE_URL}/fastapi/avatar`, 
      formData, 
      {
        baseURL: '', // baseURL 비활성화 (api 자동으로 붙이지 않음)
        headers: {
          'Content-Type': 'multipart/form-data',
        },
    });

    const rawData = response.data;

    // 매핑
    // (AI 서버의 snake_case + clothes key 값을 FE의 camelCase + clothes를 outfit으로)
    const mappedData: AvatarConfig = {
      hairFront: rawData.hair_front,
      hairBack: rawData.hair_back,
      hairColor: rawData.hair_color,
      eyes: rawData.eyes,
      glasses: rawData.glasses,
      outfit: rawData.clothes, 
      clothesColor: rawData.clothes_color
    };

    return mappedData;
  }
};