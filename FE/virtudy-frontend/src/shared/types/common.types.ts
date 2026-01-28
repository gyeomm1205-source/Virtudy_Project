// 공통 타입 파일

// 아바타 정보
export interface AvatarConfig {
  hairFront: string;
  hairBack: string;
  hairColor: string;
  eyes: string;
  glasses: string;
  outfit: string;
  clothesColor: string;
}

// 유저 정보
export interface User {
  nickName: string;
  email: string;       
  jobType: string;        
  tier: string;           
  avatar?: AvatarConfig;
  avatarImageUrl?: string; 
}