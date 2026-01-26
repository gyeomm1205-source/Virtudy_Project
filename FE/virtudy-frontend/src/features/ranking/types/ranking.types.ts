export type RankType = 'private' | 'team';

export interface RankItem {
  id: string;      // 유저 ID 또는 팀 ID
  nickName: string;// 유저 닉네임 또는 팀 이름
  rank: number;
  score: number;
  tier: string;
  profileImg?: string;
}

export interface MyRankInfo extends RankItem {
  // 내 정보에만 있는 추가 필드가 있다면 여기에 작성
}