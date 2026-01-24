package com.ssafy.virtudy.member.api;

import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.application.AuthService;
import com.ssafy.virtudy.member.dto.MemberKakaoLoginResponse;
import com.ssafy.virtudy.member.dto.MemberSignUpRequest;
import jakarta.servlet.http.HttpServlet;
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
// TODO: 회원가입, 로그인, 로그아웃, 회원탈퇴 api 모두 존재해야 함
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. 카카오 로그인 (인가 코드 전달 받음)
    @PostMapping("/kakao/callback")
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

    /**
     * 3. 로그아웃
     * - Redis 없이 순수 JWT 방식에서는 서버가 토큰을 만료시킬 방법이 없음
     * - 따라서 클라이언트의 쿠키(Refresh Token)를 삭제하는 것으로 로그아웃을 처리
     * TODO 추후 레디스로 로직 변경 검토
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String accessToken,
                                       HttpServletResponse response) {
        // 1. 토큰 파싱 ("Bearer " 제거)
        String token = resolveToken(accessToken);

        // 2. [핵심] 서비스 호출 -> Redis에서 RT 삭제 및 BL 등록
        authService.logout(token);

        // 3. 클라이언트 쿠키 삭제
        expireCookie(response, "refreshToken");
        return ResponseEntity.ok().build();
    }

    /**
     * 4. 회원탈퇴
     * - DB에서 정보를 삭제하고, 클라이언트의 쿠키도 삭제
     * - 본인 확인을 위해 Access Token을 헤더로 받는다
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@RequestHeader("Authorization") String accessToken,
                                         HttpServletResponse response) {
        // "Bearer " 제거
        String token = resolveToken(accessToken);

        // 1. 서비스: DB에서 회원 삭제
        authService.withdraw(token);

        // 2. 컨트롤러: 쿠키 삭제 (로그아웃 처리)
        expireCookie(response, "refreshToken");

        return ResponseEntity.ok().build();
    }

    /**
     * 5. 토큰 재발급 (RTR)
     * AccessToken 만료 시 호출
     */
    @PostMapping("/reissue")
    public ResponseEntity<MemberKakaoLoginResponse> reissue(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            // 리프레시 토큰이 없으면 에러
            throw new BaseException(BaseErrorCode.INVALID_TOKEN);
        }

        // 서비스 로직 (Redis 비교 및 교체)
        MemberKakaoLoginResponse result = authService.reissue(refreshToken);

        // 새 토큰 세팅
        setTokenToResponse(response, result.getAccessToken(), result.getRefreshToken());

        return ResponseEntity.ok(result);
    }

    // -------------------------------------------------------------
    // [Helper Methods]
    // -------------------------------------------------------------

    /**
     * 토큰 파싱 ("Bearer " 제거)
     * @param accessToken
     * @return
     */
    private String resolveToken(String accessToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            return accessToken.substring(7);
        }
        // 토큰이 없거나 형식이 틀리면 에러 처리 (GlobalExceptionHandler가 잡도록)
        throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
    }

    /**
     * 쿠키 만료(삭제) 메서드
     * @param response
     * @param cookieName
     */
    private void expireCookie(HttpServletResponse response, String cookieName) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "") // 값은 비움
                .path("/")
                .sameSite("Lax")
                .httpOnly(true)
                .secure(false) // 로컬 false, 배포 true
                .maxAge(0) // [중요] 수명을 0초로 설정 -> 즉시 삭제됨
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 토큰 세팅 헬퍼 메서드
     * @param response
     * @param accessToken
     * @param refreshToken
     */
    private void setTokenToResponse(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access Token -> Header
        response.setHeader("Authorization", "Bearer " + accessToken);

        // Refresh Token -> HttpOnly Cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .sameSite("Lax") // 혹은 None (HTTPS 필요)
                .httpOnly(true)
                .secure(false) // TODO 로컬 개발이라 false, 배포 시 true
                .maxAge(14 * 24 * 60 * 60) // 2주
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

}