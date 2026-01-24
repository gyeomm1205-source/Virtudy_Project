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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
                        .anyRequest().permitAll()); // TODO : 추후 정상 인증 로직 아래 버전으로 수정 필요
//                        .requestMatchers(
//                                            "/swagger-ui.html",
//                                            "/swagger-ui/**",
//                                            "/v3/api-docs/**",
//                                            "/api/auth/login",
//                                            "/api/auth/signup",
//                                            "/api/auth/reissue"
//                ).permitAll()
//                .anyRequest().authenticated())
//                .addFilterBefore(new JwtAuthFilter(jwtUtil, principalDetailsService, redisTemplate, objectMapper),
//                UsernamePasswordAuthenticationFilter.class)
=======
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
>>>>>>> c54e331 (add: oauth 회원탈퇴, 로그아웃 로직 구현)
=======
                        .anyRequest().permitAll()); //

        // 개발 중에는 아래 필터를 주석 처리하여 토큰 없이도 통과하게 만듭니다.
        // .addFilterBefore(new JwtAuthFilter(...),
        // UsernamePasswordAuthenticationFilter.class);
>>>>>>> cd0c681 (fix: 유령 세션 문제 해결 및 로컬 FE CORS 허용)
=======
                        .anyRequest().permitAll()); //

        // 개발 중에는 아래 필터를 주석 처리하여 토큰 없이도 통과하게 만듭니다.
        // .addFilterBefore(new JwtAuthFilter(...),
        // UsernamePasswordAuthenticationFilter.class);
>>>>>>> 3bd5cb7 (fix: 유령 세션 문제 해결 및 로컬 FE CORS 허용)

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // [수정] 와일드카드(*) 대신 명시적인 도메인 설정 필수!
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000", // 로컬 프론트엔드
                "http://localhost:3030", // (혹시 포트 다르면 추가)
                "http://localhost:3031", // Vite dev server
                "http://localhost:3032", // User's current port
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
