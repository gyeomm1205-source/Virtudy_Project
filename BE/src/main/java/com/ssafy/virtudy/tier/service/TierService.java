package com.ssafy.virtudy.tier.service;

import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberGameStat;
import com.ssafy.virtudy.member.repository.MemberGameStatRepository;
import com.ssafy.virtudy.member.repository.MemberRepository;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.dto.StudyAnalysisResult;
import com.ssafy.virtudy.tier.dto.TierResponse;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import com.ssafy.virtudy.study.service.StudyAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TierService {

    private final StudySessionRepository studySessionRepository;
    private final MemberGameStatRepository memberGameStatRepository;
    private final MemberRepository memberRepository;
    private final StudyAnalysisService studyAnalysisService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REDIS_TIER_KEY = "tier_ranking";
    private static final String DIAMOND = "DIAMOND";
    private static final String PLATINUM = "PLATINUM";
    private static final String GOLD = "GOLD";
    private static final String SILVER = "SILVER";
    private static final String BRONZE = "BRONZE";



    // TODO 1시간 주기로 종료된 세션만 조회하고 있는데, 세션에서 로그를 언제 남기냐에 따라 달라질 듯 
    /**
     * 1시간마다 실행되는 티어 점수 갱신 스케줄러.
     * 1. 최근 1시간 동안 종료된 세션들을 조회합니다.
     * 2. 각 멤버별로 세션을 그룹화합니다.
     * 3. 각 세션의 학습 데이터를 분석하여 티어 점수를 계산합니다.
     * 4. 계산된 점수를 DB(MemberGameStat)와 Redis(ZSet)에 업데이트합니다.
     * 매 정시(0분 0초)에 실행됩니다.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void scheduleTierUpdate() {
        log.info("티어 점수 갱신 [시작]: {}", LocalDateTime.now());

        // 1. 조회 범위 설정 (최근 1시간)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);

        // 2. 해당 시간 동안 종료된 모든 세션 조회
        List<StudySession> sessions = studySessionRepository.findByEndTimeBetween(oneHourAgo, now);

        // 3. 멤버별로 세션 그룹화 (Member -> List<StudySession>)
        Map<Member, List<StudySession>> memberSessions = sessions.stream()
                .collect(Collectors.groupingBy(StudySession::getMember));

        // 4. 각 멤버별 점수 계산 및 업데이트 로직 실행
        for (Map.Entry<Member, List<StudySession>> entry : memberSessions.entrySet()) {
            Member member = entry.getKey();
            List<StudySession> mySessions = entry.getValue();

            int totalNetTime = 0;   // 순 공부 시간 (분)
            int totalSleep = 0;     // 졸음 횟수
            int totalPhone = 0;     // 핸드폰 사용 횟수
            int totalAway = 0;      // 자리비움 횟수

            // 세션별 분석 결과 집계
            for (StudySession session : mySessions) {
                StudyAnalysisResult result = studyAnalysisService.analyzeSession(session);
                totalNetTime += result.getNetStudyTime();
                totalSleep += result.getDrowsyCount();
                totalPhone += result.getPhoneCount();
                totalAway += result.getAwayCount();
            }

            // 티어 점수 공식 적용
            int calculateScore = calculateTierScore(totalNetTime, totalSleep, totalPhone, totalAway);

            // DB에 내 점수 업데이트 (영구 저장용)
            updateMemberTierScore(member, calculateScore);
        }

        log.info("티어 점수 갱신 [종료]. 업데이트된 멤버 수: {}", memberSessions.size());
    }

    /**
     * 티어 점수 계산 공식.
     *
     * @param netStudyTimeMin 순 공부 시간 (분)
     * @param sleepCount      졸음 감지 횟수
     * @param phoneCount      핸드폰 사용 감지 횟수
     * @param awayCount       자리 비움 감지 횟수
     * @return 계산된 티어 점수 (변동분)
     */
    private int calculateTierScore(int netStudyTimeMin, int sleepCount, int phoneCount, int awayCount) {
        // 공식: (순 공부 시간 * 10) - (졸음 * 50) - (핸드폰 * 30) - (자리비움 * 20)
        int score = (netStudyTimeMin * 10)
                - (sleepCount * 50)
                - (phoneCount * 30)
                - (awayCount * 20);
        
        // 감점이 많아서 음수가 될 수도 있음 (점수 깎임)
        return score;
    }

    /**
     * DB(MemberGameStat)에 사용자의 티어 점수를 업데이트합니다.
     * 기존 점수에 누적합니다.
     */
    private void updateMemberTierScore(Member member, int newScore) {
        MemberGameStat stat = memberGameStatRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_GAME_STAT_NOT_FOUND_ERROR));

        // 기존 점수에 누적 (accumulate)
        int currentScore = stat.getTierScore();
        int updatedScore = currentScore + newScore;
        
        // 0점 미만 방지 (선택사항, 기획에 따라 다름. 일단 0점 보정)
        if (updatedScore < 0) updatedScore = 0;

        // TODO 저장 2번하고 있는건가
        stat.updateTierScore(updatedScore);
        memberGameStatRepository.save(stat);

        // Redis 랭킹 점수 즉시 업데이트 (RankService와 키 공유)
        // 주의: RankService에서는 userId(String UUID)를 Key로 사용하므로 여기서도 getMemberId()를 사용해야 함
        // TODO 여기서 랭킹 업데이트가 필요한 게 맞나?
        try {
            redisTemplate.opsForZSet().add("rank:private:season:1", member.getMemberId(), (double) updatedScore);
        } catch (Exception e) {
            log.error("Redis 티어 점수 업데이트 실패: memberId={}, score={}", member.getMemberId(), updatedScore, e);
        }
    }


    // --- 조회 로직 ---

    /**
     * 내 티어 정보를 조회합니다.
     *
     * @param memberId 조회할 회원의 ID (UUID String)
     * @return 닉네임, 점수, 티어 등급이 포함된 응답 DTO TierReponse
     * @throws BaseErrorCode.MEMBER_NOT_FOUND_ERROR 회원이 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public TierResponse getMyTier(String memberId) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR));

        // DB에서 최신 게임 스탯 조회 (없으면 0점으로 간주)
        MemberGameStat stat = memberGameStatRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_GAME_STAT_NOT_FOUND_ERROR));

        return TierResponse.builder()
                .nickname(member.getNickName())
                .tierScore(stat.getTierScore())
                .tierRank(calculateTierRank(stat.getTierScore())) // 점수 -> 등급 변환
                .totalStudyTime(stat.getTotalStudyTime())
                .build();
    }

    /**
     * 점수에 따른 티어 등급을 계산합니다.
     */
    private String calculateTierRank(int score) {
        if (score >= 100000) return DIAMOND; 
        if (score >= 70000) return PLATINUM;
        if (score >= 40000) return GOLD;
        if (score >= 20000) return SILVER;
        return BRONZE;
    }
}
