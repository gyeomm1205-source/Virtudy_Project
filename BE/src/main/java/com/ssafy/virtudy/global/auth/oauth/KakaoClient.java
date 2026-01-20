package com.ssafy.virtudy.global.auth.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoClient {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.token-uri}")
    private String tokenUri;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;


    private final RestTemplate restTemplate; // Config에 Bean 등록 필요

    /**
     * 인가 코드로 토큰 요청
     * 요청 URL: https://kauth.kakao.com/oauth/token
     * @param code: 인가 코드
     * @return access token 문자열
     */
    public String getAccessToken(String code) {
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 바디 설정 (카카오 문서에 맞춰 파라미터 추가)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        // 요청 객체 생성
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // API 호출 (POST)
        try {
            ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(
                    tokenUri,
                    request,
                    KakaoTokenResponse.class
            );
            return response.getBody().getAccessToken();
        } catch (Exception e) {
            log.error("카카오 토큰 발급 실패: {}", e.getMessage());
            throw new RuntimeException("카카오 토큰 발급 중 오류가 발생했습니다.");
        }
    }

    /**
     * 위에서 발급받은 access Token으로 사용자 정보 요청하기
     * 요청 URL: https://kapi.kakao.com/v2/user/me
     * @param accessToken: 발급받은 인가 코드
     * @return KakaoUserInfo Dto로 매핑해서 리턴한다
     */
    public KakaoUserInfo getUserInfo(String accessToken) {
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // "Authorization: Bearer {token}"
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 객체 생성 (바디는 비워도 됨)
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // API 호출 (GET)
        try {
            ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    request,
                    KakaoUserInfo.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("카카오 유저 정보 조회 실패: {}", e.getMessage());
            throw new RuntimeException("카카오 유저 정보 조회 중 오류가 발생했습니다.");
        }
    }
}