package com.ssafy.virtudy.study.dto;

import com.ssafy.virtudy.member.domain.Member;
import lombok.Getter;

@Getter
public class SessionMemberInfoResponse {
    private final String memberId;
    private final String nickName;
    private final String avatarImageUrl;

    public SessionMemberInfoResponse(Member member) {
        this.memberId = member.getMemberId();
        this.nickName = member.getNickName();
        this.avatarImageUrl = member.getAvatarImageUrl();
    }
}
