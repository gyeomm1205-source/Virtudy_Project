package com.ssafy.virtudy.global.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.virtudy.global.auth.jwt.JwtAuthFilter;
import com.ssafy.virtudy.global.auth.jwt.JwtUtil;
import com.ssafy.virtudy.global.auth.principal.PrincipalDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final PrincipalDetailsService principalDetailsService;

    // [추가] 필터에 넘겨줄 의존성 주입
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보안 해제
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 없이
                                                                                                              // JWT 사용
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 해제
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP 기본 인증 해제

                // [1] 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
//                                "/api/**",             // 로컬 테스트용
                                "/api/members/login", // 로그인
                                "/api/members/signup", // 회원가입
                                "/api/auth/**", // [추가] 카카오 등 소셜 로그인 경로 확보
                                "/swagger-ui/**", // Swagger UI
                                "/v3/api-docs/**" // API 문서
                        ).permitAll()
                        .anyRequest().authenticated())

                // [수정] JwtAuthFilter 생성자에 필요한 모든 파라미터 전달
                .addFilterBefore(new JwtAuthFilter(jwtUtil, principalDetailsService, redisTemplate, objectMapper),
                        UsernamePasswordAuthenticationFilter.class);

                // [참고] 우리가 AuthController에서 커스텀 로그아웃(/api/auth/logout)을 만들었으므로
                // 시큐리티의 기본 logout 설정은 충돌이 나거나 필요 없을 수 있습니다.
                // 헷갈리지 않게 일단 주석 처리하거나 지우는 것을 추천합니다.
                /*
                .logout(logout -> logout
                        .logoutUrl("/api/members/logout")
                        .deleteCookies("refreshToken")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200);
                        }));
                */

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // [수정] 와일드카드(*) 대신 명시적인 도메인 설정 필수!
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000", // 로컬 프론트엔드
                "http://localhost:3030", // (혹시 포트 다르면 추가)
                "http://www.virtudy.com" // 운영 프론트엔드
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        // [중요] 프론트엔드에서 Authorization 헤더(AT)를 읽을 수 있게 허용
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));

        configuration.setAllowCredentials(true); // 쿠키 허용
        configuration.setMaxAge(3600L); // Preflight 캐시 시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 CORS 설정 적용
        return source;
    }
}