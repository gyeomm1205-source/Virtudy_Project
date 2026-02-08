package com.ssafy.virtudy.member.service;

import com.ssafy.virtudy.member.domain.ActiveTimeType;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberPreference;
import com.ssafy.virtudy.member.domain.StudyTimeCategoryType;
import com.ssafy.virtudy.member.domain.StudyType;
import com.ssafy.virtudy.member.repository.MemberPreferenceRepository;
import com.ssafy.virtudy.member.repository.MemberRepository;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberPreferenceUpdateService {

    private final MemberRepository memberRepository;
    private final StudySessionRepository studySessionRepository;
    private final MemberPreferenceRepository memberPreferenceRepository;

    @Scheduled(cron = "0 0 1 * * MON")
    @Transactional
    public void updateMemberPreferences() {
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            updatePreferenceForMember(member);
        }
    }

    private void updatePreferenceForMember(Member member) {
        LocalDateTime end = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime start = end.minusWeeks(1);

        List<StudySession> sessions = studySessionRepository.findAllByMemberAndStartTimeBetween(member, start, end);

        if (sessions.isEmpty()) {
            return;
        }

        MemberPreference preference = memberPreferenceRepository.findByMember(member)
                .orElseThrow(() -> new RuntimeException("MemberPreference not found for member: " + member.getId()));

        StudyType newStudyType = calculateStudyType(sessions);
        ActiveTimeType newActiveTime = calculateActiveTime(sessions);
        StudyTimeCategoryType newAverageHours = calculateAverageHours(sessions);

        preference.updatePreference(newStudyType, preference.getTargetHours(), newActiveTime, newAverageHours);
        memberPreferenceRepository.save(preference);
    }

    private StudyType calculateStudyType(List<StudySession> sessions) {
        double averageFocusTime = sessions.stream()
                .mapToInt(StudySession::getSessionRealStudyTime)
                .average()
                .orElse(0.0);

        // sessionRealStudyTime is assumed to be in seconds.
        // 60 minutes = 3600 seconds.
        // If averageFocusTime (seconds) / 60 (to minutes) >= 60 (minutes)
        return (averageFocusTime / 60) >= 60 ? StudyType.MARATHON : StudyType.SPRINTER;
    }

    private ActiveTimeType calculateActiveTime(List<StudySession> sessions) {
        Map<ActiveTimeType, Long> timeCounts = sessions.stream()
                .map(session -> getActiveTimeType(session.getStartTime().toLocalTime()))
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        return timeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ActiveTimeType.MORNING); // Default
    }

    private ActiveTimeType getActiveTimeType(LocalTime time) {
        if (!time.isBefore(LocalTime.of(0, 0)) && time.isBefore(LocalTime.of(6, 0))) {
            return ActiveTimeType.DAWN;
        } else if (!time.isBefore(LocalTime.of(6, 0)) && time.isBefore(LocalTime.of(12, 0))) {
            return ActiveTimeType.MORNING;
        } else if (!time.isBefore(LocalTime.of(12, 0)) && time.isBefore(LocalTime.of(18, 0))) {
            return ActiveTimeType.AFTERNOON;
        } else {
            return ActiveTimeType.EVENING;
        }
    }

    private StudyTimeCategoryType calculateAverageHours(List<StudySession> sessions) {
        Map<LocalDate, List<StudySession>> sessionsByDay = sessions.stream()
                .collect(Collectors.groupingBy(session -> session.getStartTime().toLocalDate()));

        double averageDailyHours = sessionsByDay.values().stream()
                .mapToLong(this::calculateTotalStudyHoursForDay)
                .average()
                .orElse(0.0);

        return convertHoursToCategory(averageDailyHours);
    }

    private long calculateTotalStudyHoursForDay(List<StudySession> dailySessions) {
        if (dailySessions.isEmpty()) {
            return 0;
        }
        
        // Filter out sessions with null endTime to avoid NPE
        List<StudySession> validSessions = dailySessions.stream()
                .filter(s -> s.getEndTime() != null)
                .collect(Collectors.toList());

        if (validSessions.isEmpty()) {
            return 0;
        }

        LocalDateTime firstStartTime = validSessions.stream()
                .map(StudySession::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime lastEndTime = validSessions.stream()
                .map(StudySession::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (firstStartTime == null || lastEndTime == null) {
            return 0;
        }

        return Duration.between(firstStartTime, lastEndTime).toHours();
    }

    private StudyTimeCategoryType convertHoursToCategory(double hours) {
        if (hours < 3) {
            return StudyTimeCategoryType.ONE_TO_TWO;
        } else if (hours < 5) {
            return StudyTimeCategoryType.THREE_TO_FOUR;
        } else if (hours < 7) {
            return StudyTimeCategoryType.FIVE_TO_SIX;
        } else if (hours < 9) {
            return StudyTimeCategoryType.SEVEN_TO_EIGHT;
        } else {
            return StudyTimeCategoryType.OVER_NINE;
        }
    }
}
