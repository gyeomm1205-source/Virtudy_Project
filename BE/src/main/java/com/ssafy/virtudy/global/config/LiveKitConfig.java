package com.ssafy.virtudy.global.config;

import io.livekit.server.RoomServiceClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class LiveKitConfig {

    @Value("${livekit.url}")
    private String liveKitUrl;

    @Value("${livekit.api.key}")
    private String liveKitApiKey;

    @Value("${livekit.api.secret}")
    private String liveKitApiSecret;

    @Bean
    public RoomServiceClient roomServiceClient() {
        // RoomServiceClient는 HTTP/HTTPS 프로토콜을 사용해야 함
        // ws:// 또는 wss:// 로 시작하는 경우 http:// 또는 https:// 로 변환
        String httpUrl = liveKitUrl.replace("ws://", "http://").replace("wss://", "https://");
        return RoomServiceClient.create(httpUrl, liveKitApiKey, liveKitApiSecret);
    }
}
