export type RankType = 'private' | 'team';

// [수정] 백엔드 Avatar 명세에 맞춘 인터페이스
//AvatarConfig를 조합해 그려주는 컴포넌트가 있다면 그곳에 myRankInfo.avatar를 넘겨주면 됨
export interface AvatarConfig {
  hairFront: string;
  hairBack: string;
  hairColor: string;
  eyes: string;
  glasses: string;
  outfit: string;
  clothesColor: string;
}
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