package com.ssafy.virtudy.rank.service;

import com.ssafy.virtudy.rank.dto.RankDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankService {


    private final RedisTemplate<String, String> redisTemplate;

    // 랭크 조회
    public List<RankDTO.Response> loadRank() {
        String key = "rank:seanson:1";

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 9);

        if (tuples == null) return List.of();

        List<RankDTO.Response> responseList = new ArrayList<>();

        int currentRank = 1;

        // 2. 반복문 돌면서 DTO로 변환
        for (ZSetOperations.TypedTuple<String> tuple: tuples) {
            String userId = tuple.getValue();
            Double scoreVal = tuple.getScore();
            int score = (scoreVal != null) ? scoreVal.intValue() : 0;

            // 3. DTO 빌더 패턴 사용
            RankDTO.Response dto = RankDTO.Response.builder()
                    .id(userId)
                    .rank(currentRank++)
                    .score(score)
                    .tier(calculateTier(score))
                    .build();

            responseList.add(dto);
        }
        return responseList;
    }


    public RankDTO.Response getUserRank(String userId) {
        String key = "rank:season:1"; // 랭킹 키

        // 1. 내 등수 조회 (주의: 0등부터 시작함)
        // "거꾸로(reverse) 세어봐. 점수 높은 게 1등이니까"
        Long rankIndex = redisTemplate.opsForZSet().reverseRank(key, userId);

        // 2. 내 점수 조회
        Double scoreVal = redisTemplate.opsForZSet().score(key, userId);

        // 3. 예외 처리 (랭킹에 없는 유저일 경우)
        if (rankIndex == null) {
            // 랭킹에 기록이 없네요
            return null;
        }

        // 4. 결과 리턴
        return RankDTO.Response.builder()
                .id(userId)
                .rank(rankIndex.intValue() + 1)
                .score(scoreVal.intValue())
                .tier(calculateTier(scoreVal))
                .build();
    }

    private RankDTO.Tier calculateTier(double score) {
        if (score >= 1000) return RankDTO.Tier.DIAMOND;
        if (score >= 500) return RankDTO.Tier.GOLD;
        if (score >= 100) return RankDTO.Tier.SILVER;
        return RankDTO.Tier.BRONZE;
    }


}
