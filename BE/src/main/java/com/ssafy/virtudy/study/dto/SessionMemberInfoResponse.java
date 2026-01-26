package com.ssafy.virtudy.study.dto;

import com.ssafy.virtudy.member.domain.Avatar;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.dto.AvatarResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "스터디 세션 입장 응답 DTO (사용자 정보 및 LiveKit 토큰)")
@Getter
public class SessionMemberInfoResponse {
    @Schema(description = "회원 고유 ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String memberId;
<<<<<<< HEAD

    @Schema(description = "회원 닉네임", example = "싸피최고")
    private final String nickName;

    @Schema(description = "회원 아바타")
    private final AvatarResponse avatar;

    @Schema(description = "LiveKit 접속 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String liveKitToken;

    public SessionMemberInfoResponse(Member member, String liveKitToken) {
        this.memberId = member.getMemberId();
        this.nickName = member.getNickName();
        this.avatar = AvatarResponse.from(member.getAvatar());
=======
    private final String nickName;
    private final Avatar avatar;
    private final String liveKitToken;

    public SessionMemberInfoResponse(Member member, String liveKitToken) {
        this.memberId = member.getMemberId();
        this.nickName = member.getNickName();
<<<<<<< HEAD
        this.avatarImageUrl = member.getAvatarImageUrl();
>>>>>>> b9a6bab ([S14P11A703-119-websocket-livekit] Livekit 설정)
=======
        this.avatar = member.getAvatar();
>>>>>>> 188a259 (fix(rankservice) - avatar response에 추가, conflict 해결)
        this.liveKitToken = liveKitToken;
    }
}
