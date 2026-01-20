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
     * 토큰 생성 및 검증 테스트
     * Given: 사용자 ID, 만료 시간, Claims가 주어졌을 때
     * When: jwtTokenProvider.createJwtToken을 호출하여 토큰을 생성하면
     * Then: 토큰이 null이 아니어야 하고, validateOrThrow 호출 시 예외가 발생하지 않아야 함 (유효한 토큰)
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
        org.assertj.core.api.Assertions.assertThatCode(() -> jwtTokenProvider.validateOrThrow(token))
                .doesNotThrowAnyException();
    }

    /**
     * Claims 추출 테스트
     * Given: 특정 Claims(role=USER)를 포함한 토큰이 생성되었을 때
     * When: jwtTokenProvider.getClaims를 통해 Claims를 추출하면
     * Then: Subject(userId)와 커스텀 Claim(role)이 일치해야 함
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
     * UserPrincipal 변환 테스트 (UserPrincipal 로직 검증)
     * Given: Mock Member 객체가 주어졌을 때
     * When: UserPrincipal 생성자를 호출하면
     * Then: UserDetails 인터페이스의 메서드(getUsername, getPassword 등)가 Member의 정보를 올바르게
     * 반환해야 함
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
