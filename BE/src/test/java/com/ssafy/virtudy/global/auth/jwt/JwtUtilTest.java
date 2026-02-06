package com.ssafy.virtudy.global.auth.jwt;

import com.ssafy.virtudy.member.dto.MemberDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 테스트 초기화
     * ReflectionTestUtils를 사용하여 @Value 필드 값을 주입
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "ACCESS_TOKEN_EXPIRE_MIN", 30L);
        ReflectionTestUtils.setField(jwtUtil, "REFRESH_TOKEN_EXPIRE_MIN", 1440L);
    }

    /**
     * AccessToken 생성 테스트
     * Given: MemberDto 객체가 주어졌을 때
     * When: jwtUtil.createAccessToken을 호출하면
     * Then: jwtTokenProvider가 예상된 파라미터로 호출되고, 생성된 토큰이 반환되어야 함
     */
    @Test
    @DisplayName("AccessToken 생성 테스트")
    void createAccessToken_Success() {
        // given
        MemberDto member = MemberDto.builder()
                .memberId("testUser")
                .nickName("Tester")
                .avatar(null)
                .build();

        given(jwtTokenProvider.createJwtToken(any(), anyLong(), any())).willReturn("mockAccessToken");

        // when
        String token = jwtUtil.createAccessToken(member);

        // then
        assertThat(token).isEqualTo("mockAccessToken");
        // token expire time: 30 * 60 = 1800
        verify(jwtTokenProvider).createJwtToken(eq("testUser"), eq(30L * 60), any(Map.class));
    }

    /**
     * RefreshToken 생성 테스트
     * Given: MemberDto 객체가 주어졌을 때
     * When: jwtUtil.createRefreshToken을 호출하면
     * Then: jwtTokenProvider가 예상된 파라미터(긴 만료시간)로 호출되고, 생성된 토큰이 반환되어야 함
     */
    @Test
    @DisplayName("RefreshToken 생성 테스트")
    void createRefreshToken_Success() {
        // given
        MemberDto member = MemberDto.builder()
                .memberId("testUser")
                .build();

        given(jwtTokenProvider.createJwtToken(any(), anyLong(), any())).willReturn("mockRefreshToken");

        // when
        String token = jwtUtil.createRefreshToken(member);

        // then
        assertThat(token).isEqualTo("mockRefreshToken");
        // refresh token expire time: 1440 * 60 = 86400
        verify(jwtTokenProvider).createJwtToken(eq("testUser"), eq(1440L * 60), any(Map.class));
    }

    /**
     * 토큰 검증 성공 테스트
     * Given: 유효한 토큰이 주어졌을 때
     * When: jwtUtil.validateToken을 호출하면
     * Then: true가 반환되어야 하고, jwtTokenProvider.validateOrThrow가 호출되어야 함
     */
    @Test
    @DisplayName("토큰 검증 성공 테스트")
    void validateToken_Success() {
        // given
        String token = "validToken";
        // validateOrThrow가 예외를 던지지 않으면 성공으로 간주됨 (doNothing)

        // when
        boolean result = jwtUtil.validateToken(token);

        // then
        assertThat(result).isTrue();
        verify(jwtTokenProvider).validateOrThrow(token);
    }

    /**
     * 토큰 정보 추출 테스트
     * Given: Claims를 포함한 유효한 토큰이 주어졌을 때
     * When: 각 정보 추출 메서드(getLoginId, getUserName, getTokenType)를 호출하면
     * Then: Claims에 담긴 해당 값이 정확히 반환되어야 함
     */
    @Test
    @DisplayName("정보 추출 테스트")
    void extractInfo_Success() {
        // given
        String token = "validToken";
        Claims claims = new DefaultClaims(Map.of(
                "sub", "testUser",
                "userName", "Tester",
                "tokenType", "ACCESS"));
        given(jwtTokenProvider.getClaims(token)).willReturn(claims);

        // when
        String loginId = jwtUtil.getLoginId(token);
        String userName = jwtUtil.getUserName(token);
        String tokenType = jwtUtil.getTokenType(token);

        // then
        assertThat(loginId).isEqualTo("testUser");
        assertThat(userName).isEqualTo("Tester");
        assertThat(tokenType).isEqualTo("ACCESS");
    }
}
