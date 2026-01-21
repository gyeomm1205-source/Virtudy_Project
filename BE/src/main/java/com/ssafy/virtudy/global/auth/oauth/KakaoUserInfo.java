package com.ssafy.virtudy.global.auth.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * v2/user/me 요청에서 오는 json 파일의 구조
 * 즉 카카오가 주는 재료를 받는 곳
 *
 * {
 *   "id": 123456789,
 *   "kakao_account": {
 *     "email": "sample@kakao.com",
 *     "profile": {
 *       "nickname": "홍길동",
 *       "profile_image_url": "http://img.kakao.com/..."
 *     }
 *   }
 * }
 */
@Getter
@NoArgsConstructor
public class KakaoUserInfo {

    private Long id; // 카카오 고유 ID (회원 식별용)

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    public static class KakaoAccount {
        private String email;
        private Profile profile;

        @Getter
        @NoArgsConstructor
        public static class Profile {
            private String nickname;

            @JsonProperty("profile_image_url")
            private String profileImageUrl;
        }
    }
}