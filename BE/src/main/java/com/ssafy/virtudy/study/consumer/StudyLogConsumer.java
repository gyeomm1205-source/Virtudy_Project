package com.ssafy.virtudy.study.consumer;

import com.ssafy.virtudy.study.dto.StudyLogRequest;
import com.ssafy.virtudy.study.service.StudyLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudyLogConsumer {

    private final StudyLogService studyLogService;

    /**
     * 배치 리스너 (Batch Listener)
     * List<StudyLogRequest>로 받으면 알아서 배치 모드로 동작합니다.
     * application.yaml에 spring.kafka.listener.type: batch 설정 필요
     */
    @KafkaListener(topics = "study-log-topic", groupId = "virtudy-group")
    public void consume(List<StudyLogRequest> logs) {
        log.info("Kafka Batch: {} 건 수신", logs.size());

        if (logs.isEmpty()) return;

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
}
