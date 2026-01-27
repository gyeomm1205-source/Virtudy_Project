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
import com.ssafy.virtudy.report.domain.Report;
import com.ssafy.virtudy.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final ReportRepository reportRepository;
    private final MemberGameStatRepository memberGameStatRepository;
    private final RankService rankService;

    public MemberProfileResponse getProfile(Member member) {
        // 매 호출 시점의 어제 날짜 계산
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Report todayReport = reportRepository.findByMemberAndReportDate(member, yesterday)
                .orElse(null);

        MemberGameStat gameStat = memberGameStatRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_GAME_STAT_NOT_FOUND_ERROR));
        int tierScore = gameStat.getTierScore();
        String tier = rankService.calculateTier(tierScore).name();

        return MemberProfileResponse.from(member, todayReport, tierScore, tier);
    }

    @Transactional
    public void updateProfile(Member member, MemberProfileUpdateRequest request) {
        JobType jobType = request.getJobType() != null ? JobType.valueOf(request.getJobType()) : null;
        member.updateProfile(request.getNickName(), jobType);
    }
}
