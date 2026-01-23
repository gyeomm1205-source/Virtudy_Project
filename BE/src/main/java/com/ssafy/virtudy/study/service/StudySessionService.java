package com.ssafy.virtudy.study.service;

import com.ssafy.virtudy.global.config.LiveKitConfig;
import com.ssafy.virtudy.group.domain.RoomMember;
import com.ssafy.virtudy.group.repository.RoomMemberRepository;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.repository.MemberRepository;
import com.ssafy.virtudy.study.domain.RoomStatType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.dto.SessionMemberInfoResponse;
import com.ssafy.virtudy.study.repository.StudyRoomRepository;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class StudySessionService {

    private final static int MAX_USER = 6;

    private final StudySessionRepository studySessionRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final MemberRepository memberRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final LiveKitConfig liveKitConfig;

    public SessionMemberInfoResponse enterRoom(String memberId, String roomId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

        StudyRoom room = studyRoomRepository.findByRoomIdAndStatus(roomId, RoomStatType.OPEN)
                .orElseThrow(() -> new IllegalArgumentException("종료되었거나 존재하지 않는 방입니다."));

        studySessionRepository.findByMemberAndEndTimeIsNull(member).ifPresent(session -> {
            throw new IllegalStateException("이미 다른 방에 참여중입니다. 기존 방에서 먼저 퇴장해주세요.");
        });

        int currentUsers = studySessionRepository.findByRoomAndEndTimeIsNull(room).size();
        if (currentUsers >= MAX_USER) {
            throw new IllegalStateException("방이 가득 찼습니다.");
        }

        if (!roomMemberRepository.existsByMemberAndRoom(member, room)) {
            roomMemberRepository.save(RoomMember.builder()
                    .roomMemberId(UUID.randomUUID().toString())
                    .room(room)
                    .member(member)
                    .joinedAt(LocalDateTime.now())
                    .build());
        }

        StudySession newSession = StudySession.builder()
                .member(member)
                .room(room)
                .build();
        studySessionRepository.save(newSession);

        // LiveKit 토큰 생성
        AccessToken token = new AccessToken(liveKitConfig.getLiveKitApiKey(), liveKitConfig.getLiveKitApiSecret());
        token.setName(member.getNickName());
        token.setIdentity(memberId);
        token.addGrants(new RoomJoin(true), new RoomName(roomId));

        return new SessionMemberInfoResponse(member, token.toJwt());
    }

    public SessionMemberInfoResponse enterRandomRoom(String memberId) {
        List<StudyRoom> openRooms = studyRoomRepository.findAllByStatus(RoomStatType.OPEN);
        if (openRooms.isEmpty()) {
            throw new IllegalStateException("입장 가능한 방이 없습니다.");
        }

        List<StudyRoom> availableRooms = openRooms.stream()
                .filter(room -> studySessionRepository.findByRoomAndEndTimeIsNull(room).size() < MAX_USER)
                .toList();

        if (availableRooms.isEmpty()) {
            throw new IllegalStateException("입장 가능한 방이 없습니다.");
        }

        StudyRoom randomRoom = availableRooms.get(ThreadLocalRandom.current().nextInt(availableRooms.size()));

        return enterRoom(memberId, randomRoom.getRoomId());
    }

    public void exitRoom(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

        StudySession session = studySessionRepository.findByMemberAndEndTimeIsNull(member)
                .orElseThrow(() -> new IllegalStateException("현재 참여중인 방이 없습니다."));

        session.close();
    }
}
