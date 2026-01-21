package com.ssafy.virtudy.member.api;

import com.ssafy.virtudy.member.application.AuthService;
import com.ssafy.virtudy.member.dto.MemberKakaoLoginResponse;
import com.ssafy.virtudy.member.dto.MemberSignUpRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 *  http://localhost:3030/login/callback/kakao
 */
// TODO: 회원가입, 로그인, 회원정보 수정, 회원탈퇴, 마이페이지 조회 api 모두 존재해야 함
// TODO: url mapping 변경 필요
@RestController
@RequestMapping("/api/auth")
        @RequiredArgsConstructor
        public class AuthController {

            private final AuthService authService;

            // 1. 카카오 로그인 (인가 코드 전달 받음)
            @GetMapping("/kakao")
            public ResponseEntity<MemberKakaoLoginResponse> kakaoLogin(@RequestParam String code, HttpServletResponse response) {
                MemberKakaoLoginResponse result = authService.kakaoLogin(code);

        if (!result.isNeedSignup()) {
            // 로그인 성공 시 쿠키/헤더 세팅
            setTokenToResponse(response, result.getAccessToken(), result.getRefreshToken());
        }

        return ResponseEntity.ok(result);
    }

    // 2. 회원가입 (추가 정보 입력 후 호출)
    @PostMapping("/signup")
    public ResponseEntity<MemberKakaoLoginResponse> signup(@RequestBody @Valid MemberSignUpRequest request, HttpServletResponse response) {
        MemberKakaoLoginResponse result = authService.signup(request);

        // 가입 성공 시 바로 로그인 처리되므로 토큰 세팅
        setTokenToResponse(response, result.getAccessToken(), result.getRefreshToken());

        return ResponseEntity.ok(result);
    }

    // 토큰 세팅 헬퍼 메서드
    private void setTokenToResponse(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access Token -> Header
        response.setHeader("Authorization", "Bearer " + accessToken);

        // Refresh Token -> HttpOnly Cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .sameSite("Lax") // 혹은 None (HTTPS 필요)
                .httpOnly(true)
                .secure(false) // 로컬 개발이라 false, 배포 시 true
                .maxAge(14 * 24 * 60 * 60) // 2주
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}