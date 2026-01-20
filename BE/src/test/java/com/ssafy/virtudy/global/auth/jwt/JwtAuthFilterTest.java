package com.ssafy.virtudy.global.auth.jwt;

import com.ssafy.virtudy.global.auth.principal.PrincipalDetailsService;
import com.ssafy.virtudy.global.auth.principal.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PrincipalDetailsService principalDetailsService;

    @Mock
    private FilterChain filterChain;

    /**
     * 유효한 토큰이 있을 경우 인증 객체(Authentication)가 SecurityContext에 설정되는지 테스트
     * Given: Authorization 헤더에 유효한 Bearer 토큰이 있고
     * JwtUtil이 해당 토큰을 유효하다고 판단하고, "ACCESS" 타입이며, 로그인 ID를 반환할 때
     * PrincipalDetailsService가 해당 ID로 UserDetails를 반환할 때
     * When: jwtAuthFilter.doFilterInternal이 실행되면
     * Then: SecurityContextHolder에 Authentication 객체가 설정되어야 하고
     * filterChain.doFilter가 호출되어야 함
     */
    @Test
    @DisplayName("유효한 토큰이 있을 경우 인증 객체 설정")
    void doFilterInternal_ValidToken_AuthenticationSet() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.validateToken("validToken")).willReturn(true);
        given(jwtUtil.getTokenType("validToken")).willReturn("ACCESS");
        given(jwtUtil.getLoginId("validToken")).willReturn("testUser");

        UserDetails userDetails = mock(UserDetails.class);
        given(userDetails.getAuthorities()).willReturn(Collections.emptyList());
        given(principalDetailsService.loadUserByUsername("testUser")).willReturn(userDetails);

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);
        verify(filterChain).doFilter(request, response);
    }

    /**
     * 토큰이 없을 경우 인증 객체가 설정되지 않는지 테스트
     * Given: Authorization 헤더가 없는 요청이 왔을 때
     * When: jwtAuthFilter.doFilterInternal이 실행되면
     * Then: SecurityContextHolder에 Authentication 객체가 없어야 하고 (null)
     * jwtUtil.validateToken은 호출되지 않아야 하며
     * filterChain.doFilter는 호출되어야 함 (다음 필터로 진행)
     */
    @Test
    @DisplayName("토큰이 없으면 인증 객체 설정 안함")
    void doFilterInternal_NoToken_NoAuthentication() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // SecurityContext 초기화
        SecurityContextHolder.clearContext();

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    /**
     * 유효하지 않은 토큰일 경우 인증 객체가 설정되지 않는지 테스트
     * Given: Authorization 헤더에 유효하지 않은 토큰이 있고
     * JwtUtil이 해당 토큰을 유효하지 않다고(false) 판단할 때
     * When: jwtAuthFilter.doFilterInternal이 실행되면
     * Then: SecurityContextHolder에 Authentication 객체가 없어야 하고
     * filterChain.doFilter는 호출되어야 함
     */
    @Test
    @DisplayName("유효하지 않은 토큰이면 인증 객체 설정 안함")
    void doFilterInternal_InvalidToken_NoAuthentication() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalidToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.validateToken("invalidToken")).willReturn(false);

        // SecurityContext 초기화
        SecurityContextHolder.clearContext();

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
