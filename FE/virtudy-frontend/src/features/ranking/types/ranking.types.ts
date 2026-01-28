export type RankType = 'private' | 'team';
import type { AvatarConfig } from '@/shared/types/common.types';


export interface RankItem {
  id: string;          // [추가] 고유 ID (User_1 등)
  nickName: string; // [중요] 이제 이게 식별자 역할 겸 표시 이름
  rank: number;
  score: number;
  email: string;    // 본인 확인용 (유니크 키로 사용 가능)
  avatar: AvatarConfig; 
  tier: string;
}

export interface MyRankInfo extends RankItem {
}