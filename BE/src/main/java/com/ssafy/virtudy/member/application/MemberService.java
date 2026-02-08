package com.ssafy.virtudy.member.application;

import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.JobType;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberGameStat;
import com.ssafy.virtudy.member.dto.MemberProfileResponse;
import com.ssafy.virtudy.member.dto.MemberProfileUpdateRequest;
import com.ssafy.virtudy.member.repository.MemberGameStatRepository;
import com.ssafy.virtudy.rank.service.RankService;
import com.ssafy.virtudy.report.service.ReportService;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberGameStatRepository memberGameStatRepository;
    private final RankService rankService;
    private final StudySessionRepository studySessionRepository;
    private final ReportService reportService;

    public MemberProfileResponse getProfile(Member member) {
        // 오늘 날짜의 시작과 끝 시간 계산
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        // 오늘 날짜의 StudySession 조회
        List<StudySession> todaySessions = studySessionRepository.findAllByMemberAndStartTimeBetween(member, startOfDay, endOfDay);

        // 일일 순공부시간 및 집중도 계산
        int dailyPureStudyTime = reportService.calculateTotalStudyTime(todaySessions);
        int dailyFocusDepth = reportService.calculateFocusDepth(todaySessions);

        MemberGameStat gameStat = memberGameStatRepository.findByMember(member)
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_GAME_STAT_NOT_FOUND_ERROR));
        int tierScore = gameStat.getTierScore();
        String tier = rankService.calculateTier(tierScore).name();

        return MemberProfileResponse.from(member, dailyPureStudyTime, dailyFocusDepth, tierScore, tier);
    }

    @Transactional
    public void updateProfile(Member member, MemberProfileUpdateRequest request) {
        JobType jobType = request.getJobType() != null ? JobType.valueOf(request.getJobType()) : null;
        member.updateProfile(request.getNickName(), jobType);
    }
}
