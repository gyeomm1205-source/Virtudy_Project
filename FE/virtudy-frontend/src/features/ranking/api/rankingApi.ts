// import api from '@/shared/api/axios.config'; // 설정된 axios 인스턴스
// import type { RankItem, RankType } from '../types/ranking.types';

// export const getMyRank = async (type: RankType) => {
//   const { data } = await api.get<RankItem>('/v1/ranks/me', { params: { type: type.toUpperCase()} });
//   return data;
// };

// export const getRankList = async (page: number, type: RankType) => {
//   const { data } = await api.get<RankItem[]>('/v1/ranks/', {
//     params: { page, type: type.toUpperCase()}
//   });
//   return data;
// };

// export const searchRank = async (name: string, type: RankType) => {
//   const { data } = await api.get<RankItem[]>('/v1/ranks/search', {
//     params: { name, type: type.toUpperCase() }
//   });
//   return data;
// };


// import api from '@/shared/api/axios.config'; // 잠시 주석 처리
import type { RankItem, RankType } from '../types/ranking.types';

// [1] 가짜 데이터 생성 (백엔드에서 줄 거라고 예상되는 데이터)
const MOCK_MY_RANK = {
  id: "테스트유저",
  rank: 12,
  score: 3450,
  tier: "GOLD",
  profileImg: "https://via.placeholder.com/150"
};

const MOCK_RANK_LIST: RankItem[] = Array.from({ length: 10 }).map((_, i) => ({
  id: `User_${i + 1}`,
  rank: i + 1,
  score: 5000 - (i * 100),
  tier: i < 3 ? "DIAMOND" : "PLATINUM",
  profileImg: ""
}));

// [2] API 함수 수정 (서버 통신 흉내내기)
export const getMyRank = async (type: RankType) => {
  // const { data } = await api.get<RankItem>('/v1/ranks/me', ...);
  // return data;
  
  // 0.5초 뒤에 가짜 데이터 리턴 (로딩화면 테스트용)
  return new Promise<any>(resolve => {
    setTimeout(() => resolve(MOCK_MY_RANK), 500);
  });
};

export const getRankList = async (page: number, type: RankType) => {
  // 실제 API 호출 주석 처리
  
  return new Promise<RankItem[]>(resolve => {
    setTimeout(() => {
        // 페이지네이션 흉내 (페이지마다 랭킹 숫자 바뀌게)
        const startRank = page * 10;
        const list = MOCK_RANK_LIST.map((item, idx) => ({
            ...item,
            rank: startRank + idx + 1,
            id: `${type === 'private' ? 'User' : 'Team'}_${startRank + idx + 1}`
        }));
        resolve(list);
    }, 500);
  });
};

export const searchRank = async (name: string, type: RankType) => {
   return new Promise<RankItem[]>(resolve => {
    setTimeout(() => resolve([MOCK_MY_RANK]), 500);
  });
};