package com.ssafy.virtudy.member.dto;

import com.ssafy.virtudy.member.domain.JobType;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.report.domain.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원 프로필 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProfileResponse {

    @Schema(description = "회원 아바타")
    private AvatarResponse avatar;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "싸피")
    private String nickName;

    @Schema(description = "직업 유형", example = "UNIVERSITY_STUDENT")
    private JobType jobType;

    @Schema(description = "티어 점수", example = "110000")
    private int tierScore;

    @Schema(description = "티어", example = "DIAMOND")
    private String tier;

    @Schema(description = "최애 스터디방 제목", example = "알고리즘 스터디")
    private String favoriteRoomTitle;

    @Schema(description = "일일 순공부시간(단위 : 분)", example = "120")
    private int dailyPureStudyTime;

    @Schema(description = "일일 집중도", example = "60")
    private int dailyFocusDepth;

    public static MemberProfileResponse from(Member member, Report todayReport, int tierScore, String tier) {
        return MemberProfileResponse.builder()
                .avatar(AvatarResponse.from(member.getAvatar()))
                .email(member.getEmail())
                .nickName(member.getNickName())
                .jobType(member.getJobType())
                .tierScore(tierScore)
                .tier(tier)
                .favoriteRoomTitle(member.getFavoriteRoom() != null ? member.getFavoriteRoom().getTitle() : null)
                .dailyPureStudyTime(todayReport != null ? todayReport.getMaxFocusTime() : 0)
                .dailyFocusDepth(todayReport != null ? todayReport.getFocusDepth() : 0)
                .build();
    }
}
