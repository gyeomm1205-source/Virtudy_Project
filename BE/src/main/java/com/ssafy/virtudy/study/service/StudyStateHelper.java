package com.ssafy.virtudy.study.service;

import com.ssafy.virtudy.study.domain.StudyEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyStateHelper {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_STATE_KEY_PREFIX = "study:member:";
    private static final String SUFFIX_DELTA = ":delta";
    private static final String REDIS_DIRTY_MEMBERS_KEY = "study:dirty_members";

    // Field Names
    private static final String FIELD_TIME_MIN = "time_min";
    private static final String FIELD_SCORE_POINT = "score_point";
    private static final String FIELD_TIME_BUFFER = "time_buffer";
    private static final String FIELD_SCORE_BUFFER = "score_buffer";
    private static final String FIELD_LAST_TYPE = "last_type";
    private static final String FIELD_LAST_TIME = "last_time";

    /**
     * 사용자의 상태를 업데이트하고 버퍼링 로직을 수행합니다.
     */
    public void updateState(String memberId, StudyEventType newType, LocalDateTime eventTime) {
        String deltaKey = REDIS_STATE_KEY_PREFIX + memberId + SUFFIX_DELTA;

        // 1. 이전 상태 조회 (HMGET)
        Object lastTypeObj = redisTemplate.opsForHash().get(deltaKey, FIELD_LAST_TYPE);
        Object lastTimeObj = redisTemplate.opsForHash().get(deltaKey, FIELD_LAST_TIME);

        // 첫 로그이거나 상태 정보가 없으면 초기화만 하고 종료
        if (lastTypeObj == null || lastTimeObj == null) {
            updateLastState(deltaKey, newType, eventTime);
            return;
        }

        StudyEventType lastType = StudyEventType.valueOf((String) lastTypeObj);
        LocalDateTime lastTime = LocalDateTime.parse((String) lastTimeObj);

        // 2. 지속 시간 계산 (초 단위)
        long durationSeconds = Duration.between(lastTime, eventTime).getSeconds();
        if (durationSeconds < 0) durationSeconds = 0;

        // 3. 이전 상태가 'FOCUS'였다면 공부 시간 및 점수 버퍼 누적
        if (lastType == StudyEventType.FOCUS && durationSeconds > 0) {
            
            // --- Pure Time Buffer Logic ---
            Long currentTimeBuffer = redisTemplate.opsForHash().increment(deltaKey, FIELD_TIME_BUFFER, durationSeconds);
            if (currentTimeBuffer != null && currentTimeBuffer >= 60) {
                long minutesToAdd = currentTimeBuffer / 60;
                long secondsLeft = currentTimeBuffer % 60;
                
                redisTemplate.opsForHash().increment(deltaKey, FIELD_TIME_MIN, minutesToAdd);
                redisTemplate.opsForHash().put(deltaKey, FIELD_TIME_BUFFER, String.valueOf(secondsLeft)); // 덮어쓰기 (Safe for single user flow)
                // 만약 동시성 이슈가 걱정된다면 Lua Script 필요하지만, 한 유저의 로그는 순차적이라 가정.
            }

            // --- Tier Score Buffer Logic ---
            Long currentScoreBuffer = redisTemplate.opsForHash().increment(deltaKey, FIELD_SCORE_BUFFER, durationSeconds);
            if (currentScoreBuffer != null && currentScoreBuffer >= 600) { // 10분 = 600초
                long pointsToAdd = currentScoreBuffer / 600;
                long bufferLeft = currentScoreBuffer % 600;

                redisTemplate.opsForHash().increment(deltaKey, FIELD_SCORE_POINT, pointsToAdd);
                redisTemplate.opsForHash().put(deltaKey, FIELD_SCORE_BUFFER, String.valueOf(bufferLeft));
            }
            
            // 변경사항이 발생한 경우에만 Dirty Set에 추가 (DB 반영할 게 생겼을 수도 있으므로)
            // 사실 버퍼만 쌓여도 Dirty로 볼 수도 있지만, DB Flush는 min/point가 있을 때만 의미가 있음.
            // 하지만 스케줄러가 flush 할 때 min/point가 없으면 그냥 스킵하면 되므로 무조건 추가.
            redisTemplate.opsForSet().add(REDIS_DIRTY_MEMBERS_KEY, memberId);
        }

        // 4. 상태 업데이트
        updateLastState(deltaKey, newType, eventTime);
        
        // TTL 갱신
        redisTemplate.expire(deltaKey, 2, TimeUnit.HOURS);
    }

    private void updateLastState(String key, StudyEventType type, LocalDateTime time) {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_LAST_TYPE, type.name());
        map.put(FIELD_LAST_TIME, time.toString());
        redisTemplate.opsForHash().putAll(key, map);
    }
}
