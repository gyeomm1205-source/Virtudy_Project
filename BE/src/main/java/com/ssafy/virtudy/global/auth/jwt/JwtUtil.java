package com.ssafy.virtudy.global.auth.jwt;

import lombok.RequiredArgsConstructor;

import com.ssafy.virtudy.member.dto.MemberDto;
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
     * accessToken 생성 (subject = userId, tokenType = access)
     * 
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
                        "userName", member.getNickName(),
                        "userProfileImgUrl", member.getAvatarImageUrl()));
    }

    /**
     * refreshToken 생성 (subject = userId, tokenType = refresh)
     * 
     * @param member
     * @return
     */
    public String createRefreshToken(MemberDto member) {
        return jtp.createJwtToken(
                member.getMemberId(),
                REFRESH_TOKEN_EXPIRE_MIN,
                Map.of(
                        "tokenType", "REFRESH",
                        "userId", member.getMemberId()));
    }

    public boolean validateToken(String token) {
        try {
            jtp.validateOrThrow(token);
            return true; // 필터 입장에서는 되는지 안되는지만 알면 되니까
        } catch (BaseException e) {
            return false;
        }
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

}