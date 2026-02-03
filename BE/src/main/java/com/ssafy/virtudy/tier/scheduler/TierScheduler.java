package com.ssafy.virtudy.tier.scheduler;

import com.ssafy.virtudy.member.repository.MemberGameStatRepository;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TierScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberGameStatRepository memberGameStatRepository;
    private final StudySessionRepository studySessionRepository;

    private static final String REDIS_STATE_KEY_PREFIX = "study:member:";
    private static final String SUFFIX_DELTA = ":delta";
    private static final String REDIS_DIRTY_MEMBERS_KEY = "study:dirty_members";
    
    // Field Names matching StudyStateHelper
    private static final String FIELD_TIME_MIN = "time_min";
    private static final String FIELD_SCORE_POINT = "score_point";

    private static final int BATCH_SIZE = 1000;

    /**
     * 1분 주기로 Redis의 Delta(변경분)를 DB에 반영합니다.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void flushDeltaToDb() {
        // 1. Dirty Member Set 확인 (변경사항이 있는 멤버들)
        // SPOP으로 꺼내온 멤버들만 처리하여 중복 처리 방지
        List<Object> members = redisTemplate.opsForSet().pop(REDIS_DIRTY_MEMBERS_KEY, BATCH_SIZE);
        
        if (members == null || members.isEmpty()) {
            log.info("Tier Update Scheduler: 변경사항이 없습니다.");
            return;
        }

        log.info("Tier Update Scheduler: {}명의 변경사항 반영 시작", members.size());

        for (Object memberIdObj : members) {
            String memberId = (String) memberIdObj;
            processMemberDelta(memberId);
        }
    }

    private void processMemberDelta(String memberId) {
        String deltaKey = REDIS_STATE_KEY_PREFIX + memberId + SUFFIX_DELTA;

        try {
            // 1. 현재 누적된 값 읽기 (분 단위 시간, 점수)
            Object timeMinObj = redisTemplate.opsForHash().get(deltaKey, FIELD_TIME_MIN);
            Object scorePointObj = redisTemplate.opsForHash().get(deltaKey, FIELD_SCORE_POINT);

            int minutesToAdd = timeMinObj != null ? Integer.parseInt(timeMinObj.toString()) : 0;
            int pointsToAdd = scorePointObj != null ? Integer.parseInt(scorePointObj.toString()) : 0;

            if (minutesToAdd == 0 && pointsToAdd == 0) {
                log.info("Tier Update Scheduler: 0분 0점입니다.");
                return;
            }
            
            // 2. DB 업데이트
            if (minutesToAdd > 0 || pointsToAdd > 0) {
                memberGameStatRepository.accumulateStats(memberId, minutesToAdd, pointsToAdd);
            }
            
            if (minutesToAdd > 0) {
                studySessionRepository.accumulateTime(memberId, minutesToAdd);
            }

            // 3. Redis 차감
            // 처리한 만큼 차감합니다. (0으로 초기화하지 않고 HINCRBY 음수 사용)
            if (minutesToAdd > 0) {
                redisTemplate.opsForHash().increment(deltaKey, FIELD_TIME_MIN, -minutesToAdd);
            }
            if (pointsToAdd > 0) {
                redisTemplate.opsForHash().increment(deltaKey, FIELD_SCORE_POINT, -pointsToAdd);
            }

        } catch (Exception e) {
            log.error("DB Flush Failed for member: {}", memberId, e);
            // 실패 시 다시 Dirty Set에 추가하여 다음 번에 재시도
            redisTemplate.opsForSet().add(REDIS_DIRTY_MEMBERS_KEY, memberId);
        }
    }
}
