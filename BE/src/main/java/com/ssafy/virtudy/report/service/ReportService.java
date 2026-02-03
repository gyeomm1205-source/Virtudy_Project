package com.ssafy.virtudy.report.service;

import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.StudyTimeCategoryType;
import com.ssafy.virtudy.member.repository.MemberRepository;
import com.ssafy.virtudy.report.domain.Report;
import com.ssafy.virtudy.report.dto.ReportResponse;
import com.ssafy.virtudy.report.repository.ReportRepository;
import com.ssafy.virtudy.study.domain.StudyEventType;
import com.ssafy.virtudy.study.domain.StudyLog;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.repository.StudyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final StudyLogRepository studyLogRepository;
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final AiAnalysisService aiAnalysisService;



    /**
     * 주간 리포트 조회 (API 호출용)
     * 지정된 기간(시작일~종료일) 동안의 모든 일간 리포트를 조회하여 리스트로 반환합니다.
     * 이를 통해 프론트엔드에서 주간 통계나 그래프를 그릴 수 있습니다.
     *
     * @param memberId  조회할 회원의 식별자
     * @param startDate 조회 시작 날짜
     * @param endDate   조회 종료 날짜
     * @return List<ReportResponse> 기간 내 리포트 목록
     */
    public List<ReportResponse> getWeeklyReport(String memberId, LocalDate startDate, LocalDate endDate) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR));
        
        List<Report> reports = reportRepository.findByMemberAndReportDateBetween(member, startDate, endDate);
        
        // TODO startDate랑 endDate 사이의 데이터를 계산하는 로직 추가 필요
        // TODO 내부적으로 주간 

        return reports.stream()
                .map(ReportResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 일간 리포트 생성 및 저장 (Batch 시스템에서 호출)
     * 전날의 학습 세션 및 로그 데이터를 분석하여 5가지 핵심 스탯(지구력, 집중력, 안정감, 의지력, 규칙성)을 계산하고
     * AI 분석 코멘트를 생성하여 DB에 저장합니다.
     * 매일 새벽 4시에 실행됩니다.
     *
     * @param member   리포트 생성 대상 회원
     * @param date     리포트 기준 날짜 (보통 '어제')
     * @param sessions 해당 날짜에 완료된 학습 세션 목록
     */
    /**
     * 주간 리포트 생성 및 저장 (Batch 시스템 - ReportPreparationService에서 호출)
     * 지난주(월~일)의 학습 데이터를 분석하여 스탯을 계산하고, AI 분석 코멘트를 생성하여 DB에 저장합니다.
     *
     * @param member    리포트 생성 대상 회원
     * @param startDate 조회 시작 날짜 (지난주 월요일)
     * @param endDate   조회 종료 날짜 (지난주 일요일)
     * @param sessions  해당 기간의 학습 세션 목록
     */
    @Transactional
    public void generateAndSaveWeeklyReport(Member member, LocalDate startDate, LocalDate endDate, List<StudySession> sessions) {
        // 이미 해당 기간의 주간 리포트(보통 시작일을 기준 날짜로 함)가 존재하면 스킵
        if (reportRepository.existsByMemberAndReportDate(member, startDate)) {
            log.info("이미 주간 리포트가 존재합니다. memberId={}, date={}", member.getMemberId(), startDate);
            return;
        }

        // 해당 기간 내의 모든 로그 조회 (집중도, 졸음 등 분석용)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<StudyLog> logs = studyLogRepository.findByMemberIdAndDateRange(member.getId(), startDateTime, endDateTime);
        logs.sort((a, b) -> a.getDetectedAt().compareTo(b.getDetectedAt()));

        // --- 유효성 검증: 실제로 '7일 치' 데이터가 존재하는지 확인 ---
        long distinctStudyDays = sessions.stream()
                .map(s -> s.getEndTime().toLocalDate())
                .filter(d -> !d.isBefore(startDate) && !d.isAfter(endDate))
                .distinct()
                .count();

        // 7일 개수가 안 차면 '0점 리포트' 생성 (기본값)
        if (distinctStudyDays < 7) {
            log.info("데이터 부족으로 기본 리포트 생성 (학습일수: {}/7). memberId={}", distinctStudyDays, member.getMemberId());
            saveDefaultReport(member, startDate);
            return;
        }

        // --- 통계 계산 시작 ---
        int maxFocusTime = calculateMaxFocusTime(sessions, logs);
        int endurance = calculateEndurance(member, maxFocusTime);
        int focusDepth = calculateFocusDepth(sessions);
        int stability = calculateStability(logs);
        int willPower = calculateWillPower(sessions, logs);
        int totalStudyTime = calculateTotalStudyTime(sessions); // 총 공부시간
        int focusDepthPercentage = calculateFocusDepthPercentage(focusDepth, maxFocusTime);

        String sleepVulnerableTime = analyzeSleepPattern(logs);
        String distractionPatternTime = analyzeDistractionPattern(logs);
        String aiComment = aiAnalysisService.generateFeedback(endurance, focusDepth, stability, willPower, sleepVulnerableTime);

        Report report = Report.builder()
                .member(member)
                .reportDate(startDate)
                .focusDepthPercentage(focusDepthPercentage)
                .totalStudyTime(totalStudyTime)
                .endurance(endurance)
                .focusDepth(focusDepth)
                .regularity(0)
                .stability(stability)
                .willPower(willPower)
                .maxFocusTime(maxFocusTime)
                .sleepVulnerableTime(sleepVulnerableTime)
                .distractionPatternTime(distractionPatternTime)
                .aiComment(aiComment)
                .build();

        reportRepository.save(report);
        log.info("주간 리포트 생성 완료: memberId={}, weekStart={}", member.getMemberId(), startDate);
    }

    private void saveDefaultReport(Member member, LocalDate startDate) {
        Report report = Report.builder()
                .member(member)
                .reportDate(startDate)
                .focusDepthPercentage(0)
                .totalStudyTime(0)
                .endurance(0)
                .focusDepth(0)
                .regularity(0)
                .stability(0)
                .willPower(0)
                .maxFocusTime(0)
                .sleepVulnerableTime("데이터 없음")
                .distractionPatternTime("데이터 없음")
                .aiComment("지난 주 학습 데이터가 부족하여 분석할 수 없습니다.")
                .build();
        reportRepository.save(report);
    }

    // --- 신규 스탯 계산 로직 ---

    /**
     * 일 평균 공부 시간 (분)
     * 총 공부 시간 합계 / 7일
     */
    private int calculateTotalStudyTime(List<StudySession> sessions) {
        if (sessions.isEmpty()) return 0;
        long totalStudyTime = sessions.stream()
                .mapToLong(StudySession::getSessionRealStudyTime)
                .sum();
        return (int) (totalStudyTime);
    }

    /**
     * 집중도 백분위 (0~100)
     * 현재는 집중력 점수를 그대로 반환 (추후 고도화)
     */
    private int calculateFocusDepthPercentage(int focusDepth, int maxFocusTime) {
        return Math.min(focusDepth, 100);
    }

    // --- 스탯 계산 로직 (Private Helper Methods) ---

    /**
     * 1. 지구력 (Endurance) 계산
     * 공식: (최대 연속 집중 시간 / 사용자 목표 공부 시간) * 100
     * 사용자가 설정한 목표 시간 대비, 한 번에 얼마나 길게 집중했는지를 평가합니다.
     */
    private int calculateEndurance(Member member, int maxContinuousFocusMin) {
        // MemberPreference에서 targetHours 가져오기 (설정이 없으면 기본 60분)
        int targetMinutes = 60; 
        if (member.getPreferences() != null && !member.getPreferences().isEmpty()) {
            // 편의상 첫 번째 선호도 설정을 사용
            StudyTimeCategoryType type = member.getPreferences().get(0).getTargetHours();
            targetMinutes = convertTargetHoursToMinutes(type);
        }

        // 점수 계산 (최대 100점)
        int score = (int) ((double) maxContinuousFocusMin / targetMinutes * 100);
        return Math.min(score, 100);
    }

    // Enum 타입을 실제 분(minute) 단위 정수로 변환하는 헬퍼 메서드
    private int convertTargetHoursToMinutes(StudyTimeCategoryType type) {
        if (type == null) return 60;
        switch (type) {
            case ONE_TO_TWO: return 120; // 1~2시간 -> 최대값인 120분 기준
            case THREE_TO_FOUR: return 240;
            case FIVE_TO_SIX: return 360;
            case SEVEN_TO_EIGHT: return 480;
            case OVER_NINE: return 600; // 9시간 이상 -> 600분(10시간)으로 가정
            default: return 60;
        }
    }

    /**
     * 2. 집중력 (Focus Depth) 계산
     * 공식: (총 순 공부 시간 / 총 세션 체류 시간) * 100
     * 책상에 앉아있던 시간 중 실제로 집중(RealStudyTime)한 비율을 의미합니다.
     */
    private int calculateFocusDepth(List<StudySession> sessions) {
        long totalDurationMin = 0;
        long totalNetStudyMin = 0;
        
        for (StudySession session : sessions) {
            // 세션 전체 시간 (Start ~ End)
            long duration = Duration.between(session.getStartTime(), session.getEndTime()).toMinutes();
            totalDurationMin += duration;
            // 순 공부 시간 (StudyLogAnalysis 결과)
            totalNetStudyMin += session.getSessionRealStudyTime();
        }

        if (totalDurationMin == 0) return 0; // 0으로 나누기 방지

        int score = (int) ((double) totalNetStudyMin / totalDurationMin * 100);
        return Math.min(score, 100);
    }

    /**
     * 3. 안정감 (Stability) 계산
     * 공식: 100 - (졸음 감지 횟수 * 5)
     * 졸음 상태 구간(Interval)의 개수를 셉니다.
     * 예: FOCUS -> SLEEP -> SLEEP -> FOCUS 인 경우, SLEEP 구간은 1회입니다.
     */
    private int calculateStability(List<StudyLog> logs) {
        int sleepIntervals = 0;
        StudyEventType lastType = null;

        for (StudyLog log : logs) {
            if (log.getEventType() == StudyEventType.SLEEP) {
                // 이전 타입이 SLEEP이 아니었다면 새로운 졸음 구간 시작
                if (lastType != StudyEventType.SLEEP) {
                    sleepIntervals++;
                }
            }
            lastType = log.getEventType();
        }
        
        // 졸음 구간 1회당 5점 감점 (최소 0점)
        int score = 100 - (sleepIntervals * 5);
        return Math.max(0, score);
    }

    /**
     * 4. 의지력 (Willpower) 계산
     * 공식: 100 - (딴짓 감지 횟수 * 5)
     * 딴짓(PHONE, AWAY) 상태 구간의 개수를 셉니다.
     */
    private int calculateWillPower(List<StudySession> sessions, List<StudyLog> logs) {
        int distractionIntervals = 0;
        boolean wasDistracted = false; 
        for (StudyLog log : logs) {
            boolean isDistraction = (log.getEventType() == StudyEventType.PHONE || log.getEventType() == StudyEventType.AWAY);
            
            if (isDistraction) {
                // 이전 타입이 딴짓 타입이 아니면 카운트
                if (!wasDistracted) {
                    distractionIntervals++;
                }
                wasDistracted = true;
            } else {
                wasDistracted = false;
            }
        }
        
        if (distractionIntervals == 0) return 100;
        
        // 딴짓 구간 1회당 5점 감점
        int score = 100 - (distractionIntervals * 5);
        return Math.max(0, score);
    }

    /**
     * 5. 최대 집중 시간 (Max Focus Time) 계산
     * 하루 동안 '방해 요소(졸음, 폰, 이탈)' 없이 지속된 가장 긴 집중 구간을 찾습니다.
     * @return 분 단위 시간
     */
    private int calculateMaxFocusTime(List<StudySession> sessions, List<StudyLog> logs) {
        int maxFocusSec = 0;
        
        // 각 세션별로 분석 수행
        for (StudySession session : sessions) {
            LocalDateTime sessionStart = session.getStartTime();
            LocalDateTime sessionEnd = session.getEndTime();
            
            // 해당 세션에 속하는 로그만 필터링
            List<StudyLog> sessionLogs = logs.stream()
                    .filter(log -> log.getSession().getId().equals(session.getId()))
                    .sorted((a, b) -> a.getDetectedAt().compareTo(b.getDetectedAt()))
                    .collect(Collectors.toList());

            LocalDateTime lastFocusStart = sessionStart;
            boolean isFocusing = true; // 세션 시작 시점은 집중 상태로 가정

            for (StudyLog log : sessionLogs) {
                if (isFocusing) {
                    // 집중 상태에서 방해 이벤트 발생 -> 집중 구간 종료 및 시간 계산
                    if (isDistraction(log.getEventType())) {
                        int duration = (int) Duration.between(lastFocusStart, log.getDetectedAt()).toSeconds();
                        if (duration > maxFocusSec) maxFocusSec = duration;
                        isFocusing = false; // 상태 변경: 집중 X
                    }
                } else {
                    // 방해 상태에서 FOCUS 이벤트 발생 -> 다시 집중 구간 시작
                    if (log.getEventType() == StudyEventType.FOCUS) {
                        lastFocusStart = log.getDetectedAt();
                        isFocusing = true; // 상태 변경: 집중 O
                    }
                }
            }
            
            // 세션 종료 시점까지 집중이 유지되었다면 마지막 구간 계산
            if (isFocusing) {
                 int duration = (int) Duration.between(lastFocusStart, sessionEnd).toSeconds();
                 if (duration > maxFocusSec) maxFocusSec = duration;
            }
        }
        return maxFocusSec / 60; // 초 -> 분 변환
    }

    // 해당 이벤트 타입이 '집중 방해 요소'인지 판별
    private boolean isDistraction(StudyEventType type) {
        return type == StudyEventType.PHONE || type == StudyEventType.AWAY || type == StudyEventType.SLEEP;
    }

    /**
     * 6. 졸음 취약 시간대 분석
     * 하루 중 'SLEEP' 이벤트가 가장 많이 발생한 시간(Hour)을 찾습니다.
     */
    private String analyzeSleepPattern(List<StudyLog> logs) {
        return findMostFrequentHour(logs, StudyEventType.SLEEP, "발견되지 않음");
    }

    /**
     * 7. 딴짓 빈번 시간대 분석
     * 하루 중 'PHONE' 이벤트가 가장 많이 발생한 시간(Hour)을 찾습니다.
     */
    private String analyzeDistractionPattern(List<StudyLog> logs) {
        return findMostFrequentHour(logs, StudyEventType.PHONE, "발견되지 않음");
    }

    // 특정 이벤트 타입이 가장 많이 발생한 시간을 찾는 로직 (공통화)
    private String findMostFrequentHour(List<StudyLog> logs, StudyEventType targetType, String defaultMsg) {
        Map<Integer, Integer> hourCounts = new HashMap<>();
        
        for (StudyLog log : logs) {
            // PHONE 패턴 분석 시 AWAY도 포함할 수도 있지만, 현재는 PHONE만 혹은 파라미터에 따름
            if (log.getEventType() == targetType) {
                int hour = log.getDetectedAt().getHour();
                hourCounts.put(hour, hourCounts.getOrDefault(hour, 0) + 1);
            }
        }

        if (hourCounts.isEmpty()) return defaultMsg;

        // 빈도수가 가장 높은 시간(Key) 추출
        return hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> String.format("%02d시", entry.getKey()))
                .orElse(defaultMsg);
    }
}
