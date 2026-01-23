package com.ssafy.virtudy.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.ssafy.virtudy.member.domain.Member;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    private String memberId;
    private String nickName;
    private String avatarImageUrl;
    private String email;

    // Entity -> Dto 변환 메서드
    public static MemberDto from(Member member) {
        return MemberDto.builder()
                .memberId(member.getMemberId())
                .nickName(member.getNickName())
                .email(member.getEmail())
                .avatarImageUrl(member.getAvatarImageUrl())
                .build();
    }
}
