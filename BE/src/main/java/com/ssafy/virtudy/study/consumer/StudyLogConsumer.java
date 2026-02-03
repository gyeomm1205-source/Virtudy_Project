package com.ssafy.virtudy.study.consumer;

import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.dto.StudyLogRequest;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import com.ssafy.virtudy.study.service.StudyLogService;
import com.ssafy.virtudy.study.service.StudyStateHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudyLogConsumer {

    private final StudyLogService studyLogService;
    private final StudyStateHelper studyStateHelper;
    private final StudySessionRepository studySessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_SESSION_KEY_PREFIX = "study:session:";
    private static final String REDIS_SESSION_KEY_SUFFIX = ":info";

    /**
     * 배치 리스너 (Batch Listener)
     * List<StudyLogRequest>로 받으면 알아서 배치 모드로 동작합니다.
     * application.yaml에 spring.kafka.listener.type: batch 설정 필요
     */
    @KafkaListener(topics = "study-log-topic", groupId = "virtudy-group")
    public void consume(List<StudyLogRequest> logs) {
        log.info("Kafka Batch: {} 건 수신", logs.size());

        if (logs.isEmpty()) return;

        // 1. 실시간 상태 업데이트 및 델타 누적 (Redis - Helper 위임)
        for (StudyLogRequest logItem : logs) {
            try {
                processRealTimeState(logItem);
            } catch (Exception e) {
                log.error("실시간 상태 업데이트 실패: sessionId={}", logItem.getSessionId(), e);
                // 실시간 처리가 실패해도 로그 저장은 계속 진행
            }
        }

        // 로그 내용 확인 (테스트용 - 디버그 레벨)
        if (log.isDebugEnabled()) {
            logs.forEach(logItem -> {
                log.debug("Session: {}, Event: {}, Time: {}",
                        logItem.getSessionId(), logItem.getEventType(), logItem.getDetectedAt());
            });
        }

        // Service 계층으로 넘겨서 DB 저장 처리
        studyLogService.saveBatch(logs);
    }

    private void processRealTimeState(StudyLogRequest logItem) {
        String sessionId = logItem.getSessionId();
        String redisKey = REDIS_SESSION_KEY_PREFIX + sessionId + REDIS_SESSION_KEY_SUFFIX;

        // Redis에서 MemberId 조회
        String memberId = (String) redisTemplate.opsForValue().get(redisKey);

        // Redis에 없으면 DB에서 조회 (Fallback)
        if (memberId == null) {
            StudySession session = studySessionRepository.findBySessionId(sessionId).orElse(null);
            if (session != null) {
                memberId = session.getMember().getMemberId();
                // 다시 Redis에 캐싱 (복구)
                redisTemplate.opsForValue().set(redisKey, memberId, 24, TimeUnit.HOURS);
            }
        }

        if (memberId != null) {
            studyStateHelper.updateState(memberId, logItem.getEventType(), logItem.getDetectedAt());
        } else {
            log.warn("세션 정보를 찾을 수 없어 상태 업데이트 무시: sessionId={}", sessionId);
        }
    }
}
