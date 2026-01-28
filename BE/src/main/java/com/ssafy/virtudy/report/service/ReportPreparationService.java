package com.ssafy.virtudy.report.service;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportPreparationService {

    private final StudySessionRepository studySessionRepository;
    private final ReportService reportService;

    /**
     * 주간 리포트 생성 스케줄러
     * 매주 월요일 새벽 4시 0분 0초 (cron = "0 0 4 * * MON")에 실행됩니다.
     * 지난주(월~일)의 학습 데이터를 집계하여 리포트를 생성합니다.
     */
    @Scheduled(cron = "0 0 4 * * MON")
    public void createWeeklyReports() {
        // 지난주 월요일 ~ 일요일 기간 설정
        LocalDate today = LocalDate.now();
        LocalDate lastMonday = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        LocalDate lastSunday = today.minusWeeks(1).with(java.time.DayOfWeek.SUNDAY);

        LocalDateTime startOfWeek = lastMonday.atStartOfDay();
        LocalDateTime endOfWeek = lastSunday.atTime(23, 59, 59);

        log.info("주간 리포트 생성 배치 시작 [기간: {} ~ {}]", lastMonday, lastSunday);

        // 1. 해당 주간에 학습 기록이 있는 모든 세션 조회
        List<StudySession> sessions = studySessionRepository.findByEndTimeBetween(startOfWeek, endOfWeek);

        // 2. 멤버별로 세션 데이터 그룹화 (Map<Member, List<StudySession>>)
        Map<Member, List<StudySession>> memberSessions = sessions.stream()
                .collect(Collectors.groupingBy(StudySession::getMember));

        log.info("리포트 생성 대상: 총 {}명의 회원", memberSessions.size());

        // 3. 각 멤버별로 주간 리포트 생성 서비스 호출
        for (Map.Entry<Member, List<StudySession>> entry : memberSessions.entrySet()) {
            Member member = entry.getKey();
            List<StudySession> mySessions = entry.getValue();
            
            try {
                // 주간 리포트 생성 (기간 정보 전달)
                reportService.generateAndSaveWeeklyReport(member, lastMonday, lastSunday, mySessions);
            } catch (Exception e) {
                log.error("리포트 생성 실패 - memberId: {}, error: {}", member.getMemberId(), e.getMessage());
            }
        }

        log.info("주간 리포트 생성 배치 종료");
    }
}
