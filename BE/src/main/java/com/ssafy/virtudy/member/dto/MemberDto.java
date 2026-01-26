package com.ssafy.virtudy.member.dto;

import com.ssafy.virtudy.member.domain.Avatar;
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
    private Avatar avatar;
    private String email;

    // Entity -> Dto 변환 메서드
    public static MemberDto from(Member member) {
        return MemberDto.builder()
                .memberId(member.getMemberId())
                .nickName(member.getNickName())
                .email(member.getEmail())
                .avatar(member.getAvatar())
                .build();
    }
}
