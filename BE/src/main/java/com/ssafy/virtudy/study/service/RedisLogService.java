package com.ssafy.virtudy.study.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.StudyLog;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.dto.StudyLogRequest;
import com.ssafy.virtudy.study.repository.StudyLogRepository;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLogService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final StudySessionRepository studySessionRepository;
    private final StudyLogRepository studyLogRepository;

    private static final String REDIS_LOG_KEY = "study:log:queue";
    private static final int BATCH_SIZE = 1000; // 한 번의 스케줄에 최대 몇 개까지 처리할지 (무한 루프 방지)

    /**
     * Redis Queue에 쌓인 학습 로그를 꺼내서 DB에 저장합니다.
     * 스케줄러(TierService)에 의해 1시간마다 호출됩니다.
     */
    @Transactional
    public void processPendingLogs() {
        log.info("Redis 로그 동기화 시작: {}", LocalDateTime.now());

        List<StudyLog> logsToSave = new ArrayList<>();
        int count = 0;

        // Redis List에서 데이터를 하나씩 꺼내옵니다 (FIFO: Left Pop)
        // AI 서버가 RPUSH 했다고 가정
        while (count < BATCH_SIZE) {
            // pop을 하면 Redis에서 해당 데이터는 삭제됨
            Object data = redisTemplate.opsForList().leftPop(REDIS_LOG_KEY);
            
            if (data == null) {
                break; // 더 이상 데이터가 없음
            }

            try {
                // JSON String -> DTO
                String jsonString = (String) data; // RedisTemplate 설정에 따라 String 저장됨
                StudyLogRequest message = objectMapper.readValue(jsonString, StudyLogRequest.class);

                // DTO -> Entity 변환
                StudyLog studyLog = convertToEntity(message);
                if (studyLog != null) {
                    logsToSave.add(studyLog);
                }
            } catch (Exception e) {
                log.error("Redis 로그 파싱/변환 실패: data={}", data, e);
                // 실패한 데이터는 버리거나, 별도 실패 큐(Dead Letter Queue)에 넣는 정책 필요
                // 여기서는 일단 로그 찍고 넘어감
            }
            
            count++;
        }

        if (!logsToSave.isEmpty()) {
            studyLogRepository.saveAll(logsToSave);
            log.info("Redis 로그 동기화 완료: {}건 DB 저장", logsToSave.size());
        } else {
            log.info("Redis 로그 동기화 완료: 처리할 데이터 없음");
        }
    }

    private StudyLog convertToEntity(StudyLogRequest message) {
        // 1. 세션 조회 (없으면 유효하지 않은 로그 -> 무시)
        String sessionId = message.getSessionId();
        StudySession session = studySessionRepository.findBySessionId(sessionId)
                .orElse(null);

        if (session == null) {
            // 이미 종료된지 오래되었거나 잘못된 세션 ID
            log.warn("유효하지 않은 세션 로그 무시: sessionId={}", sessionId);
            return null;
        }

        Member member = session.getMember();

        // 2. Entity Builder
        return StudyLog.builder()
                .logId(UUID.randomUUID().toString())
                .session(session)
                .member(member)
                .eventType(message.getEventType())
                .detectedAt(message.getDetectedAt() != null ? message.getDetectedAt() : LocalDateTime.now())
                .build();
    }
}
