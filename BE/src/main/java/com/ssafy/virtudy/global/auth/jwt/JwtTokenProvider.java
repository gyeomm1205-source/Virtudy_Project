package com.ssafy.virtudy.global.auth.jwt;

import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct; // Spring Boot 3.x (javax -> jakarta)
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret_key}")
    private String secretKey;

    private Key key;

    /**
     * 객체 초기화: secretKey를 Base64로 디코딩하여 Key 객체에 저장 (1회만 실행)
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 토큰 생성
     */
    public String createJwtToken(String userId, long expireMin, Map<String, Object> claims) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + 1000 * 60 * expireMin);

        return Jwts.builder()
                .setSubject(userId)
                .addClaims(claims) // 주의: claims 맵에 'sub' 키가 있으면 위 setSubject와 덮어쓰기 경합이 일어날 수 있음
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰 파싱 및 Claims 추출
     * - 이 메서드 자체가 검증 로직을 포함함 (서명 불일치 시 예외 발생)
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이어도 claims를 반환하고 싶다면 e.getClaims()를 리턴 (선택사항)
            throw new BaseException(BaseErrorCode.TOKEN_EXPIRED);
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            // 위변조, 구조 문제 등
            throw new BaseException(BaseErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰 유효성 검사 (Boolean 반환)
     * - 단순히 "통과냐 아니냐"만 궁금할 때 사용
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token); // 위에서 만든 메서드 재활용
            return true;
        } catch (Exception e) {
            // 여기서는 예외를 던지지 않고 false만 리턴하여 필터에서 처리하게 할 수도 있음
            return false;
        }
    }
}