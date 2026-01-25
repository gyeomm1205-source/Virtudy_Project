import type { RankItem, RankType, MyRankInfo } from '../types/ranking.types';

// 실제 백엔드 통신 시 주석 해제
// import api from '@/shared/api/axios.config';

// --- Mock Data (모의 데이터) ---
// 백엔드 개발이 완료되면 아래 Mock 데이터를 사용하는 부분을 실제 API 호출 코드로 교체

const MOCK_MY_PRIVATE_RANK: MyRankInfo = {
  id: "user123",
  nickName: "김싸피",
  rank: 12,
  score: 3450,
  tier: "GOLD",
  profileImg: "https://i.pravatar.cc/150?u=user123"
};

const MOCK_MY_TEAM_RANK: MyRankInfo = {
  id: "teamA703",
  nickName: "칠공삼(703)",
  rank: 3,
  score: 15000,
  tier: "DIAMOND",
  profileImg: "https://i.pravatar.cc/150?u=teamA703"
};

// --- API Functions (API 함수) ---

// 내 랭킹 정보 조회 (Get My Rank Info)
export const getMyRank = async (type: RankType): Promise<MyRankInfo> => {
  console.log(`[Mock API] getMyRank called with type: ${type}`);
  // 실제 백엔드 통신 코드
  /*
  const { data } = await api.get<MyRankInfo>('/v1/ranks/me', { params: { type } });
  return data;
  */
  // Mock 구현 
  return new Promise(resolve => {
    setTimeout(() => {
      if (type === 'private') {
        resolve(MOCK_MY_PRIVATE_RANK);
      } else {
        resolve(MOCK_MY_TEAM_RANK);
      }
    }, 300);
  });
};

// 랭킹 리스트 조회 
export const getRankList = async (page: number, type: RankType): Promise<RankItem[]> => {
  console.log(`[Mock API] getRankList called with page: ${page}, type: ${type}`);
  // 실제 백엔드 통신 코드 
  /*
  const { data } = await api.get<RankItem[]>('/v1/ranks/', {
    params: { page, type }
  });
  return data;
  */
  // Mock 구현 
  return new Promise(resolve => {
    setTimeout(() => {
      const startRank = page * 10;
      const MOCK_RANK_LIST: RankItem[] = Array.from({ length: 10 }).map((_, i) => {
        const currentRank = startRank + i + 1;
        const isPrivate = type === 'private';
        return {
          id: isPrivate ? `user_${currentRank}` : `team_${currentRank}`,
          nickName: `${isPrivate ? '유저' : '팀'}_${currentRank}`,
          rank: currentRank,
          score: 5000 - (currentRank * 100),
          tier: currentRank < 4 ? "DIAMOND" : "PLATINUM",
        }
      });
      resolve(MOCK_RANK_LIST);
    }, 300);
  });
};

// 랭킹 검색 
export const searchRank = async (name: string, type: RankType): Promise<RankItem[]> => {
  console.log(`[Mock API] searchRank called with name: ${name}, type: ${type}`);
  // 실제 백엔드 통신 코드 (Actual backend communication code)
  /*
  const { data } = await api.get<RankItem[]>('/v1/ranks/search', {
    params: { name, type }
  });
  return data;
  */
  // Mock 구현 
  const searchResultItem: RankItem = {
      id: type === 'private' ? 'search_user' : 'search_team',
      nickName: name,
      rank: 1,
      score: 9999,
      tier: 'DIAMOND',
  };
   return new Promise(resolve => {
    setTimeout(() => resolve([searchResultItem]), 300);
  });
};

// [추가] 상위 5명 랭킹 조회 (Top 5) - Mock 구현
export const getTop5Rank = async (type: RankType): Promise<RankItem[]> => {
  console.log(`[Mock API] getTop5Rank called with type: ${type}`);

  // 실제 백엔드 통신 코드 
  /*
  const { data } = await api.get<RankItem[]>('/ranks/top5', { 
    params: { type } 
  });
  return data;
  */

  // Mock 구현: 강제로 1등~5등 데이터 5개 생성해서 리턴
  return new Promise(resolve => {
    setTimeout(() => {
      const mockTop5 = Array.from({ length: 5 }).map((_, i) => {
        const isPrivate = type === 'private';
        return {
          id: isPrivate ? `top_user_${i}` : `top_team_${i}`,
          // 닉네임을 좀 더 그럴듯하게 (예: 랭킹왕_1, 랭킹팀_1)
          nickName: isPrivate ? `랭킹왕_${i + 1}` : `최강팀_${i + 1}`, 
          rank: i + 1,
          score: 20000 - (i * 1000), // 점수 높게 설정
          tier: 'DIAMOND',
          profileImg: ''
        };
      });
      resolve(mockTop5);
    }, 300); // 0.3초 뒤에 응답 옴
  });
};