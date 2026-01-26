package com.ssafy.virtudy.global.auth.jwt;

import lombok.RequiredArgsConstructor;

import com.ssafy.virtudy.member.dto.MemberDto;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ssafy.virtudy.global.event.exception.BaseException;

import io.jsonwebtoken.Claims;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtTokenProvider jtp;


    // Value는 static 필드에 주입되지 않는다는 점
    @Value("${jwt.access-expmin}")
    private long ACCESS_TOKEN_EXPIRE_MIN;
    @Value("${jwt.refresh-expmin}")
    private long REFRESH_TOKEN_EXPIRE_MIN;

    /**
     * [JWT 발급: AT] accessToken 생성 (subject = userId, tokenType = access)
     * @param member
     * @return
     */
    public String createAccessToken(MemberDto member) {
        return jtp.createJwtToken(
                member.getMemberId(),
                ACCESS_TOKEN_EXPIRE_MIN,
                Map.of(
                        "tokenType", "ACCESS",
                        "userId", member.getMemberId(),
                        "email", member.getEmail(),
                        "userName", member.getNickName(),
<<<<<<< HEAD
<<<<<<< HEAD
                        "userProfileImgUrl", member.getAvatar()
=======
                        "userProfileImgUrl", member.getAvatarImageUrl()
>>>>>>> c54e331 (add: oauth 회원탈퇴, 로그아웃 로직 구현)
=======
                        "userProfileImgUrl", member.getAvatar()
>>>>>>> 188a259 (fix(rankservice) - avatar response에 추가, conflict 해결)
                ));
    }

    /**
     * [JWT 발급: RT]
     * refreshToken 생성 (subject = userId, tokenType = refresh, email = email)
     * @param member RT를 생성할 멤버 대상
     * @return 생성한 RT
     */
    public String createRefreshToken(MemberDto member) {
        return jtp.createJwtToken(
                member.getMemberId(),
                REFRESH_TOKEN_EXPIRE_MIN,
                Map.of(
                        "tokenType", "REFRESH",
                        "userId", member.getMemberId(),
                        "email", member.getEmail() // ✅ [필수 추가] 이게 있어야 reissue에서 꺼내 씁니다!
                ));
    }

    /**
     * [JWT 토큰 검증]
     * @param token ACCESS, REFRESH
     * @return 유효한 토큰인지 TRUE, FALSE
     */
    public boolean validateToken(String token) {
        try {
            jtp.validateOrThrow(token);
            return true; // 필터 입장에서는 되는지 안되는지만 알면 되니까
        } catch (BaseException e) {
            return false;
        }
    }

    /**
     * [로그아웃]
     * Access Token의 남은 유효 시간만큼만 블랙리스트에 넣기 위해 남은 시간을 계산하는 메서드
     * @param token Access 토큰
     * @return AT의 남은 시간
     */
    public long getExpiration(String token) {
        Date expiration = jtp.getClaims(token).getExpiration();
        long now = new Date().getTime();
        return expiration.getTime() - now;
    }

    public String getLoginId(String token) {
        return jtp.getClaims(token).getSubject();
    }

    public String getUserName(String token) {
        Claims claims = jtp.getClaims(token);
        return (String) claims.get("userName");
    }

    public String getUserProfileImgUrl(String token) {
        Claims claims = jtp.getClaims(token);
        return (String) claims.get("userProfileImgUrl");
    }

    public String getTokenType(String token) {
        Claims claims = jtp.getClaims(token);
        return (String) claims.get("tokenType");
    }

    public String getEmail(String token) {
        Claims claims = jtp.getClaims(token);
        return (String) claims.get("email");
    }
}