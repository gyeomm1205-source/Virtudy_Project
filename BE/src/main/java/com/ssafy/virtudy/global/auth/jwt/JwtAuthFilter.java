package com.ssafy.virtudy.global.auth.jwt;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ssafy.virtudy.global.auth.principal.PrincipalDetailsService;
import com.ssafy.virtudy.global.auth.principal.UserPrincipal;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.dto.MemberDto;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Jwt 인증 필터
 * "/login" 이외의 URI 요청이 왔을 때 처리하는 필터
 *
 * 기본적으로 사용자는 요청 헤더에 AccessToken만 담아서 요청
 * AccessToken 만료 시에만 RefreshToken을 요청 헤더에 AccessToken과 함께 요청
 *
 * 1. RefreshToken이 없고, AccessToken이 유효한 경우 -> 인증 성공 처리, RefreshToken을 재발급하지는
 * 않는다.
 * 2. RefreshToken이 없고, AccessToken이 없거나 유효하지 않은 경우 -> 인증 실패 처리, 403 ERROR
 * 3. RefreshToken이 있는 경우 -> DB의 RefreshToken과 비교하여 일치하면 AccessToken 재발급,
 * RefreshToken 재발급(RTR 방식)
 * 인증 성공 처리는 하지 않고 실패 처리
 *
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final PrincipalDetailsService principalDetailsService;
    private final StringRedisTemplate redisTemplate; // [추가] Redis 주입
    private final ObjectMapper objectMapper; // Spring이 자동으로 주입해줌 (JSON 변환기)

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

////         1. 토큰 유효성 검사
        if (token != null && jwtUtil.validateToken(token)) {

            // [추가] 2. Redis 블랙리스트 확인
            // "BL:" + token 키가 존재하면 로그아웃된 토큰임
            String isLogout = redisTemplate.opsForValue().get("BL:" + token);
            if (isLogout != null) {
                // 로그아웃된 토큰 요청 시 401 에러 혹은 다음 필터 진행 안 함
                // 명시적으로 에러 전달
                jwtExceptionHandler(response, BaseErrorCode.INVALID_TOKEN);
                return;
            }

            String tokenType = jwtUtil.getTokenType(token);
            if ("ACCESS".equals(tokenType)) {
                String memberId = jwtUtil.getLoginId(token);
                // DB 조회 없이 JWT에서 정보 추출하여 인증 객체 생성 가능하면 성능상 이득
                // 하지만 여기서는 PrincipalDetailsService가 있으므로 활용하거나
                // 간단히 JWT 정보로만 UserPrincipal을 만들 수도 있음.
                // PrincipalDetailsService를 사용하면 DB 조회가 발생하지만 정보가 확실함.

                // 방법 1: PrincipalDetailsService 사용 (DB 조회 발생)
                UserDetails userDetails = principalDetailsService.loadUserByUsername(memberId);

                // Authentication 객체 생성
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * [필터 에러] JSON으로 응답
     * @param response
     * @param errorCode
     */
    public void jwtExceptionHandler(HttpServletResponse response, BaseErrorCode errorCode) {
        response.setStatus(errorCode.getStatus().value()); // 401 or 400 등
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // BaseResponse나 ErrorResponse 객체 모양에 맞춰 JSON 생성
            // 예시: {"code": "AUTH_002", "message": "유효하지 않은 토큰입니다."}
            String json = objectMapper.writeValueAsString(Map.of(
                    "code", errorCode.name(),
                    "message", errorCode.getMessage()
            ));
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * [request 분해] 토큰 꺼내기
     * @param request
     * @return
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
