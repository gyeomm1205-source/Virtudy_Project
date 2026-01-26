package com.ssafy.virtudy.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class StompHeaderChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // STOMP CONNECT 요청일 때 헤더에서 memberId와 roomId를 추출하여 세션 속성에 저장
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String memberId = accessor.getFirstNativeHeader("memberId");
            String roomId = accessor.getFirstNativeHeader("roomId");
            log.info("STOMP Connect: memberId={}, roomId={}", memberId, roomId);

            if (memberId != null && roomId != null) {
                Objects.requireNonNull(accessor.getSessionAttributes()).put("memberId", memberId);
                accessor.getSessionAttributes().put("roomId", roomId);
            }
        }
        return message;
    }
}
