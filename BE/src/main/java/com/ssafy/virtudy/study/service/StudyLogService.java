package com.ssafy.virtudy.study.service;

import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.StudyLog;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.dto.StudyLogRequest;
import com.ssafy.virtudy.study.repository.StudyLogRepository;
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
}
