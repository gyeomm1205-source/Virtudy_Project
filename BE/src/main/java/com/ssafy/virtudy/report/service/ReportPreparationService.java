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
     * 일간 리포트 생성 스케줄러
     * 매일 새벽 4시 0분 0초 (cron = "0 0 4 * * *")에 실행됩니다.
     * 새벽 시간대는 서버 트래픽이 적고, 사용자들이 전날 공부를 확실히 마친 시점이므로 배치를 돌리기에 적합합니다.
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void createDailyReports() {
        // 전날(yesterday) 데이터를 집계 대상으로 설정
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);

        log.info("일간 리포트 생성 배치 시작 [기준일: {}]", yesterday);

        // 1. 어제 학습 기록이 있는 모든 세션 조회
        // (세션이 끝나지 않은 경우도 있을 수 있지만, EndTime이 해당 범위에 있는 세션만 대상으로 함)
        List<StudySession> sessions = studySessionRepository.findByEndTimeBetween(startOfDay, endOfDay);

        // 2. 멤버별로 세션 데이터 그룹화 (Map<Member, List<StudySession>>)
        // DB를 여러 번 왔다 갔다 하지 않고, 한 번의 조회와 메모리 상 그룹핑으로 효율성 증대
        Map<Member, List<StudySession>> memberSessions = sessions.stream()
                .collect(Collectors.groupingBy(StudySession::getMember));

        log.info("리포트 생성 대상: 총 {}명의 회원", memberSessions.size());

        // 3. 각 멤버별로 리포트 생성 서비스 호출
        for (Map.Entry<Member, List<StudySession>> entry : memberSessions.entrySet()) {
            Member member = entry.getKey();
            List<StudySession> mySessions = entry.getValue();
            
            try {
                // 개별 회원의 리포트 생성 중 에러가 나더라도, 다른 회원 작업은 계속되어야 함 -> try-catch
                reportService.generateAndSaveDailyReport(member, yesterday, mySessions);
            } catch (Exception e) {
                log.error("리포트 생성 실패 - memberId: {}, error: {}", member.getMemberId(), e.getMessage());
                // 필요 시 슬랙 알림이나 별도 에러 테이블 저장 등 추가 조치 가능
            }
        }

        log.info("일간 리포트 생성 배치 종료");
    }
}
