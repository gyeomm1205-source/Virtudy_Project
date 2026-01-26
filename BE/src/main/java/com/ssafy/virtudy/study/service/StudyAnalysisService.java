package com.ssafy.virtudy.study.service;

import com.ssafy.virtudy.study.domain.StudyEventType;
import com.ssafy.virtudy.study.domain.StudyLog;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.dto.StudyAnalysisResult;
import com.ssafy.virtudy.study.repository.StudyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyAnalysisService {

    private final StudyLogRepository studyLogRepository;

    /**
     * 특정 학습 세션(Session)에 대한 통계를 분석합니다.
     * <p>
     * 해당 세션 동안 발생한 로그들을 조회하여 감점 요인(졸음, 폰, 이탈) 횟수를 카운트하고,
     * 세션의 순 공부 시간을 포함한 분석 결과를 반환합니다.
     * </p>
     * 
     * @param session 분석할 학습 세션 엔티티
     * @return 분석된 결과 DTO (순공부시간, 감점요인 횟수 등)
     */
    public StudyAnalysisResult analyzeSession(StudySession session) {
        // 1. 해당 세션의 모든 로그 조회
        List<StudyLog> logs = studyLogRepository.findBySessionId(session.getId());

        int drowsyCount = 0;
        int phoneCount = 0;
        int awayCount = 0;

        // 2. 로그 타입별 횟수 카운트
        for (StudyLog log : logs) {
            if (log.getEventType() == StudyEventType.SLEEP) {
                drowsyCount++;
            } else if (log.getEventType() == StudyEventType.PHONE) {
                phoneCount++;
            } else if (log.getEventType() == StudyEventType.AWAY) {
                awayCount++;
            }
        }

        int totalTimeMin = 0;
        if (session.getEndTime() != null) {
            totalTimeMin = (int) Duration.between(session.getStartTime(), session.getEndTime()).toMinutes();
        }

        // 3. 세션 엔티티에 저장된 순 공부 시간 가져오기
        // (세션 종료 시점에 클라이언트/서버 로직에 의해 이미 계산되어 저장된 값)
        int netTimeMin = session.getSessionRealStudyTime();

        // 4. 결과 DTO 빌드 및 반환
        return StudyAnalysisResult.builder()
                .totalStudyTime(totalTimeMin)
                .netStudyTime(netTimeMin)
                .drowsyCount(drowsyCount)
                .phoneCount(phoneCount)
                .awayCount(awayCount)
                .build();
    }
}
