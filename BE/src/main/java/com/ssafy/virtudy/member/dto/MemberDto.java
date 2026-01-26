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

    // TODO 여기에 Member 관련 데이터가 다 들어갈 필요는 없겠지
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
