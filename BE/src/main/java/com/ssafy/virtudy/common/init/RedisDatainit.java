package com.ssafy.virtudy.common.init;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberGameStat;
import com.ssafy.virtudy.member.dto.MemberDto;
import com.ssafy.virtudy.member.dto.MemberGameStatDto;
import com.ssafy.virtudy.member.repository.MemberGameStatRepository;
import com.ssafy.virtudy.member.repository.MemberRepository;
import com.ssafy.virtudy.study.domain.StudyRoom;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 서버 시작 시 기존 Rank 관련 Redis Data를 삭제하고, Rank 줄 세우기 시작 후 다시 넣기
 */
@Component
@AllArgsConstructor
@Profile("local")
@Order(2)
@Slf4j
public class RedisDatainit implements ApplicationRunner {
    private final StringRedisTemplate redisTemplate;
    private final MemberGameStatRepository memberGameStatRepository;
    private final MemberRepository memberRepository;

    private static final String RANK_PRIVATE_KEY = "rank:private:season:1";
    private static final String RANK_TEAM_KEY = "rank:team:season:1";
    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // 1. DB조회
        List<MemberGameStat> memberGameStatList = memberGameStatRepository.findAll();

        List<Member> memberList = memberRepository.findAll();

        Map<String, Member> teamScoreMap = new HashMap<>();

        for (Member member : memberList) {
            teamScoreMap.put(member.getMemberId(), member);
        }

        List<MemberGameStatDto.RedisResponse> redisPrivateResponses = new ArrayList<>();
        List<MemberGameStatDto.RedisResponse> redisTeamResponses = new ArrayList<>();
        // 루프 돌면서 Dto에 박기 .
        for(MemberGameStat memberGameStat : memberGameStatList) {
            redisPrivateResponses.add(
                    MemberGameStatDto.RedisResponse.builder()
                    .id(memberGameStat.getMember().getMemberId())
                    .nickName(memberGameStat.getMember().getNickName())
                    .email(memberGameStat.getMember().getEmail())
                    .tierScore(memberGameStat.getTierScore())
                    .build()
            );

            StudyRoom studyRoom = teamScoreMap.get(memberGameStat.getMember().getMemberId()).getFavoriteRoom();
            // DB접근이 너무 많으니까 -> 따로 뺴기
            // 최애팀 찾고 그 팀의 아이디와 스코어 갖고 오기.

            redisTeamResponses.add(
                    MemberGameStatDto.RedisResponse.builder()
                            .id(studyRoom.getRoomId())
                            .nickName(studyRoom.getTitle())
                            .email(studyRoom.getOwner().getEmail())
                            .tierScore(studyRoom.getRoomTierScore())
                            .build()
            );
        }

        // 2. redis 청소
        redisTemplate.delete(RANK_PRIVATE_KEY);
        redisTemplate.delete(RANK_TEAM_KEY);


        // 3. redis 적재
        // 개인 등수 저장
        for(MemberGameStatDto.RedisResponse response: redisPrivateResponses) {
            redisTemplate.opsForZSet().add(RANK_PRIVATE_KEY, response.getId(), response.getTierScore());
        }

        for(MemberGameStatDto.RedisResponse response : redisTeamResponses) {
            redisTemplate.opsForZSet().add(RANK_TEAM_KEY, response.getId(), response.getTierScore());
        }



        log.info("✅ Redis 더미 데이터 생성 완료!");
    }
}
