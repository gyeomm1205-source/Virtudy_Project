package com.ssafy.virtudy.global.config;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.repository.MemberRepository;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHeaderChannelInterceptor implements ChannelInterceptor {

    private final MemberRepository memberRepository;
    private final StudySessionRepository studySessionRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // STOMP CONNECT 요청일 때 헤더에서 memberId와 roomId를 추출하여 세션 속성에 저장
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String memberId = accessor.getFirstNativeHeader("memberId");
            String roomId = accessor.getFirstNativeHeader("roomId");
            log.info("STOMP Connect: memberId={}, roomId={}", memberId, roomId);

            if (memberId != null && roomId != null) {
                // 중복 입장 방지 로직
                Member member = memberRepository.findByMemberId(memberId).orElse(null);
                if (member != null) {
                    Optional<StudySession> activeSession = studySessionRepository.findByMemberAndEndTimeIsNull(member);
                    if (activeSession.isPresent()) {
                        String activeRoomId = activeSession.get().getRoom().getRoomId();
                        if (activeRoomId.equals(roomId)) {
                            log.warn("User {} is already in room {}", memberId, roomId);
                            throw new MessagingException("User is already in this room.");
                        }
                    }
                }

                Objects.requireNonNull(accessor.getSessionAttributes()).put("memberId", memberId);
                accessor.getSessionAttributes().put("roomId", roomId);
            }
        }
        // 2. [퇴장] DISCONNECT 요청일 때: 프론트가 준 study-time을 세션에 '잠깐 저장'
        // (주의: 강제 종료 시에는 이 if문을 타지 않음 -> null 처리됨)
        else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            String studyTime = accessor.getFirstNativeHeader("study-time");

            if (studyTime != null) {
                log.info("STOMP Disconnect (Normal Exit): study-time={}", studyTime);
                // 여기서 넣어줘야 Listener에서 꺼내 쓸 수 있음!
                Objects.requireNonNull(accessor.getSessionAttributes()).put("study-time", studyTime);
            }
        }
        return message;
    }
}
