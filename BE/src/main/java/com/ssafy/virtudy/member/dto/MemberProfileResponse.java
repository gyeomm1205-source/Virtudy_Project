package com.ssafy.virtudy.member.dto;

import com.ssafy.virtudy.member.domain.Avatar;
import com.ssafy.virtudy.member.domain.JobType;
import com.ssafy.virtudy.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 *  TODO: 아바타 이미지 URL 형식 변경
 *  TODO: 티어 스코어, 최애 스터디방 제목 필드 추가
 */
@Schema(description = "회원 프로필 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProfileResponse {

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

    public static MemberProfileResponse from(Member member) {
        return MemberProfileResponse.builder()
                .avatar(member.getAvatar())
                .email(member.getEmail())
                .nickName(member.getNickName())
                .jobType(member.getJobType())
                .tier("DIAMOND") // 임시 고정값
                .miniReport("Mini Report") // 임시 고정값
                .build();
    }
}
