import type { RankItem, RankType, MyRankInfo } from '../types/ranking.types';
import api from '@/shared/api/axios.config';

// [수정] 내 랭킹 정보 조회
export const getMyRank = async (type: RankType): Promise<MyRankInfo> => {
  const { data } = await api.get<MyRankInfo>('/ranks/me', { params: { type } });
  return data; 
};

// [수정] 랭킹 리스트 조회
export const getRankList = async (page: number, type: RankType): Promise<RankItem[]> => {
  const { data } = await api.get<RankItem[]>('/ranks', {
    params: { page, type }
  });
  return data; 
};

// [수정] 랭킹 검색
export const searchRank = async (name: string, type: RankType): Promise<RankItem[]> => {
  const { data } = await api.get<RankItem[]>('/ranks/search', {
    params: { name, type }
  });
  return data; 
};

// [수정] 상위 5명 조회
export const getTop5Rank = async (type: RankType): Promise<RankItem[]> => {
  const { data } = await api.get<RankItem[]>('/ranks/top5', {
    params: { type }
  });
  return data; 
};