package com.ssafy.virtudy.member.dto;

import com.ssafy.virtudy.member.domain.Avatar;
import com.ssafy.virtudy.member.domain.JobType;
import com.ssafy.virtudy.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProfileResponse {
    private Avatar avatar;
    private String email;
    private String nickName;
    private JobType jobType;
    private String tier;
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
