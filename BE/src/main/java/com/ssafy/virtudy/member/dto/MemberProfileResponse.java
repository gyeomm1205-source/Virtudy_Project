package com.ssafy.virtudy.member.dto;

import com.ssafy.virtudy.member.domain.Avatar;
import com.ssafy.virtudy.member.domain.JobType;
import com.ssafy.virtudy.member.domain.Member;
<<<<<<< HEAD
import com.ssafy.virtudy.report.domain.Report;
=======
>>>>>>> 6e9e953 ([S14P11A703-106] API 명세서 구체화)
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

<<<<<<< HEAD
=======
/*
 *  TODO: 아바타 이미지 URL 형식 변경
 *  TODO: 티어 스코어, 최애 스터디방 제목 필드 추가
 */
>>>>>>> 6e9e953 ([S14P11A703-106] API 명세서 구체화)
@Schema(description = "회원 프로필 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProfileResponse {
<<<<<<< HEAD
<<<<<<< HEAD
=======
=======

>>>>>>> 6e9e953 ([S14P11A703-106] API 명세서 구체화)
    private Avatar avatar;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "싸피")
    private String nickName;

    @Schema(description = "직업 유형", example = "UNIVERSITY_STUDENT")
    private JobType jobType;

    @Schema(description = "티어", example = "DIAMOND")
    private String tier;

    @Schema(description = "미니 리포트", example = "Mini Report")
    private String miniReport;
>>>>>>> 188a259 (fix(rankservice) - avatar response에 추가, conflict 해결)

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

    @Schema(description = "순공부시간(단위 : 분)", example = "120")
    private int pureStudyTime;

    @Schema(description = "집중도", example = "60")
    private int focusDepth;

    public static MemberProfileResponse from(Member member, Report todayReport, int tierScore, String tier) {
        return MemberProfileResponse.builder()
<<<<<<< HEAD
                .avatar(AvatarResponse.from(member.getAvatar()))
=======
                .avatar(member.getAvatar())
>>>>>>> 188a259 (fix(rankservice) - avatar response에 추가, conflict 해결)
                .email(member.getEmail())
                .nickName(member.getNickName())
                .jobType(member.getJobType())
                .tierScore(tierScore)
                .tier(tier)
                .favoriteRoomTitle(member.getFavoriteRoom() != null ? member.getFavoriteRoom().getTitle() : null)
                .pureStudyTime(todayReport != null ? todayReport.getMaxFocusTime() : 0)
                .focusDepth(todayReport != null ? todayReport.getFocusDepth() : 0)
                .build();
    }
}
