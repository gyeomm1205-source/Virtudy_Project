package com.ssafy.virtudy.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberKakaoLoginResponse {
    private boolean needSignup; // true면 회원가입 페이지로 이동시켜라
    private String accessToken; // 로그인 성공 시 JWT (needSignup=false일 때만 존재)
    private String refreshToken;

    // 가입 필요한 경우 프론트에 채워줄 정보 (needSignup=true일 때만 존재)
    private String email;
    private String tempNickname;
    private String tempProfileImg;
}