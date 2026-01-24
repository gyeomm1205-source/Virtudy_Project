package com.ssafy.virtudy.rank;

import com.ssafy.virtudy.rank.domain.Rank;
import com.ssafy.virtudy.rank.repository.RankRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
@ActiveProfiles("local")
public class RankDummyDataTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String RANK_PRIVATE_KEY = "rank:private:season:1";
    private static final String RANK_TEAM_KEY = "rank:team:season:1";

    @Test
    void createDummyData() {
        redisTemplate.delete(RANK_PRIVATE_KEY);
        redisTemplate.delete(RANK_TEAM_KEY);
        Random random = new Random();

        // 1. 50명의 유저 데이터 생성
        for (int i = 1; i <= 50; i++) {
            String userId = "User_" + i; // 유저 ID (Value)

            // 점수 생성 로직
            // i가 1일 때(상위권) 점수가 높고, 50일 때(하위권) 점수가 낮도록 설정
            double baseScore = 5000 - (i * 90);
            double randomVariation = random.nextInt(50); // 약간의 랜덤성 추가
            double finalScore = baseScore + randomVariation;

            if (finalScore < 0) finalScore = 0; // 점수가 음수면 0으로

            // [핵심] ZSet 저장 명령어: ZADD rank:season:1 점수 유저ID
            // *주의*: 티어와 등수는 저장하지 않습니다. Redis가 점수 기반으로 알아서 계산해줍니다.
            redisTemplate.opsForZSet().add(RANK_PRIVATE_KEY, userId, finalScore);
        }

        // 1. 50명의 팀 데이터 생성
        for (int i = 1; i <= 50; i++) {
            String userId = "Team_" + i; // 팀 ID (Value)

            // 점수 생성 로직
            // i가 1일 때(상위권) 점수가 높고, 50일 때(하위권) 점수가 낮도록 설정
            double baseScore = 5000 - (i * 90);
            double randomVariation = random.nextInt(50); // 약간의 랜덤성 추가
            double finalScore = baseScore + randomVariation;

            if (finalScore < 0) finalScore = 0; // 점수가 음수면 0으로

            // [핵심] ZSet 저장 명령어: ZADD rank:season:1 점수 유저ID
            // *주의*: 티어와 등수는 저장하지 않습니다. Redis가 점수 기반으로 알아서 계산해줍니다.
            redisTemplate.opsForZSet().add(RANK_TEAM_KEY, userId, finalScore);
        }

        System.out.println("✅ Redis ZSet(Sorted Set)에 더미 데이터 50개 저장 완료!!");
        System.out.println("👉 Key 이름: " + RANK_PRIVATE_KEY);
        System.out.println("👉 Key 이름: " + RANK_TEAM_KEY);
    }

}
