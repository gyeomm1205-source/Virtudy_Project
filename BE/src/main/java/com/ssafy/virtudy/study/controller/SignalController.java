package com.ssafy.virtudy.study.controller;

import com.ssafy.virtudy.study.dto.SignalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SignalController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 및 방 제어(Control) 메시지를 해당 방의 모든 참여자에게 전달합니다.
     * 클라이언트는 /pub/signal/{roomId} 로 메시지를 발행합니다.
     * 서버는 /sub/room/{roomId} 를 구독하는 모든 클라이언트에게 메시지를 브로드캐스트합니다.
     *
     * @param message 채팅 또는 제어 메시지 (type, sender, data 등)
     * @param roomId  스터디방의 고유 ID
     */
    @MessageMapping("/signal/{roomId}")
    public void handleSignal(@Payload SignalMessage message, @DestinationVariable String roomId) {
        log.info("Control/Chat message from {} in room {}: type={}", message.getSender(), roomId, message.getType());

        // 메시지를 해당 방을 구독하는 모든 클라이언트에게 전송
        messagingTemplate.convertAndSend("/sub/room/" + roomId, message);
    }
}
