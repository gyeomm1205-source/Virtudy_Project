package com.ssafy.virtudy.study.service;

import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.StudyLog;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.dto.StudyLogRequest;
import com.ssafy.virtudy.study.repository.StudyLogRepository;
import com.ssafy.virtudy.study.repository.StudyLogBulkRepository;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyLogService {

    private final StudyLogRepository studyLogRepository;
    private final StudyLogBulkRepository studyLogBulkRepository; // [신규] JDBC (저장용)
    private final StudySessionRepository studySessionRepository;

    /**
     * 학습 로그(이벤트)를 저장합니다.
     * <p>
     * AI 모델 혹은 클라이언트로부터 전달받은 감지 이벤트(졸음, 폰, 이탈 등)를 DB에 저장합니다.
     * </p>
     *
     * @param request 로그 저장 요청 DTO (세션ID, 이벤트타입, 감지시간)
     * @return 저장된 로그의 PK (ID)
     * @throws BaseException 유효하지 않은 세션 ID일 경우 예외 발생
     */
    public Long saveLog(StudyLogRequest request) {

        // 1. 해당 세션이 실제로 존재하는지 확인 (없으면 Custom Exception 발생)
        StudySession session = studySessionRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new BaseException(BaseErrorCode.SESSION_NOT_FOUND_ERROR));

        // 2. 세션에 연결된 멤버 정보 조회
        Member member = session.getMember();

        // 3. 로그 엔티티 생성
        StudyLog log = StudyLog.builder()
                .logId(UUID.randomUUID().toString())
                .session(session)
                .member(member)
                .eventType(request.getEventType())
                .detectedAt(request.getDetectedAt() != null ? request.getDetectedAt() : LocalDateTime.now()) // 감지 시간 없으면 현재 시간
                .build();

        // 4. DB 저장
        studyLogRepository.save(log);
        return log.getId();
    }

    /**
     * Kafka Batch Consumer용: 여러 로그를 한 번에 저장합니다.
     * - JDBC Bulk Insert (batchUpdate)를 사용하여 성능을 최적화했습니다.
     * - DB URL에 rewriteBatchedStatements=true 옵션이 있어야 실제 Bulk Insert로 동작합니다.
     *
     * @param requests 로그 요청 리스트
     */
    public void saveBatch(java.util.List<StudyLogRequest> requests) {
        if (requests == null || requests.isEmpty()) return;

        // 1. DTO -> Entity 변환
        java.util.List<StudyLog> logs = requests.stream()
            .map(req -> {
                 // getReferenceById는 PK(Long)를 요구하므로, String sessionId로는 사용할 수 없습니다.
                 // 따라서 findBySessionId로 조회합니다. (캐싱 도입 시 최적화 가능)
                 StudySession session = studySessionRepository.findBySessionId(req.getSessionId())
                         .orElseThrow(() -> new BaseException(BaseErrorCode.SESSION_NOT_FOUND_ERROR));
                 
                 return StudyLog.builder()
                        // .logId(...) 생략: @Builder.Default로 자동 생성됨
                        .session(session)
                        .member(session.getMember())
                        .eventType(req.getEventType())
                        .detectedAt(req.getDetectedAt() != null ? req.getDetectedAt() : LocalDateTime.now())
                        .build();
            })
            // 스트림 내부에서 예외 발생 시 전체 중단하지 않으려면 filter/try-catch 처리 필요하나, 
            // 현재 구조에선 Entity 변환 실패 시 런타임 예외가 터져서 롤백되는 것이 정합성 유지에 나을 수 있음 
            // 또는 개별 try-catch로 감싸서 유효한 것만 필터링 가능
            .collect(java.util.stream.Collectors.toList());

        // 2. [수정] JDBC Bulk Insert 호출
        studyLogBulkRepository.saveAllBatch(logs);
    }
}
