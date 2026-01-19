package com.ssafy.virtudy.global.auth.jwt;

import com.ssafy.virtudy.global.auth.principal.UserPrincipal;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberStatType;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String secretKey = "thisisatestsecretkeyformedforjwttokenproviderandneeds tobelongenough";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // ReflectionTestUtils to inject private field (imitating @Value)
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", encodedKey);
        jwtTokenProvider.init();
    }

    /**
     * 토큰 생성 테스트
     * - 유효한 토큰이 생성되는지 확인
     * - 생성된 토큰이 validateToken()을 통과하는지 확인
     */
    @Test
    void createJwtToken_ShouldReturnValidToken() {
        // Given
        String userId = "123";
        long expireMin = 10;
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");

        // When
        String token = jwtTokenProvider.createJwtToken(userId, expireMin, claims);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    /**
     * Claims 추출 테스트
     * - 토큰에 담긴 정보(Subject, Claims)가 정상적으로 복호화되어 추출되는지 확인
     */
    @Test
    void getClaims_ShouldReturnCorrectClaims() {
        // Given
        String userId = "123";
        long expireMin = 10;
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");
        String token = jwtTokenProvider.createJwtToken(userId, expireMin, claims);

        // When
        Claims extractedClaims = jwtTokenProvider.getClaims(token);

        // Then
        assertThat(extractedClaims.getSubject()).isEqualTo(userId);
        assertThat(extractedClaims.get("role")).isEqualTo("USER");
    }

    /**
     * UserPrincipal 변환 테스트
     * - Member 엔티티가 UserPrincipal(UserDetails 구현체)로 올바르게 매핑되는지 확인
     * - getUsername()이 memberId를 반환하는지 등 검증
     */
    @Test
    void userPrincipal_ShouldReturnCorrectValues() {
        // Given
        Member member = mock(Member.class);
        when(member.getMemberId()).thenReturn("testuser");
        when(member.getPassword()).thenReturn("password");
        when(member.getStatus()).thenReturn(MemberStatType.ACTIVE);

        // When
        UserPrincipal userPrincipal = new UserPrincipal(member);

        // Then
        assertThat(userPrincipal.getUsername()).isEqualTo("testuser");
        assertThat(userPrincipal.getPassword()).isEqualTo("password");
        assertThat(userPrincipal.isEnabled()).isTrue();
    }
}
