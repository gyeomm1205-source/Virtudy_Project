package com.ssafy.virtudy.study.dto;

import com.ssafy.virtudy.member.domain.Avatar;
import com.ssafy.virtudy.member.domain.Member;
import lombok.Getter;

@Getter
public class SessionMemberInfoResponse {
    private final String memberId;
    private final String nickName;
    private final Avatar avatar;
    private final String liveKitToken;

    public SessionMemberInfoResponse(Member member, String liveKitToken) {
        this.memberId = member.getMemberId();
        this.nickName = member.getNickName();
        this.avatar = member.getAvatar();
        this.liveKitToken = liveKitToken;
    }
}
