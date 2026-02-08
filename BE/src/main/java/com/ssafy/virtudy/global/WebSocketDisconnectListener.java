package com.ssafy.virtudy.global;

import com.ssafy.virtudy.global.config.StompHeaderChannelInterceptor;
import com.ssafy.virtudy.study.dto.SignalMessage;
import com.ssafy.virtudy.study.service.StudySessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {

    private final StudySessionService studySessionService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final StompHeaderChannelInterceptor stompHeaderChannelInterceptor;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null) {
            String memberId = (String) sessionAttributes.get("memberId");
            String roomId = (String) sessionAttributes.get("roomId");

            if (memberId != null && roomId != null) {
                log.info("User disconnected: memberId={}, roomId={}", memberId, roomId);
                
                // 메모리에서 사용자 제거
                stompHeaderChannelInterceptor.removeUser(memberId);

                // DB에 퇴장 시간 기록
                studySessionService.exitRoom(memberId);

                // 다른 참여자들에게 퇴장 사실 알림
                SignalMessage leaveMessage = new SignalMessage("leave", memberId, null, null);
                messagingTemplate.convertAndSend("/sub/room/" + roomId, leaveMessage);
            }
        }
    }
}
