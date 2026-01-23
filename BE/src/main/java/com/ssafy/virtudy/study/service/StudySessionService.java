package com.ssafy.virtudy.study.service;

import com.ssafy.virtudy.global.config.LiveKitConfig;
<<<<<<< HEAD
import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
=======
>>>>>>> e788f78 ([S14P11A703-136] 엔티티 최신화)
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
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
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

    public SessionMemberInfoResponse enterRoom(Member member, String roomId) {
        StudyRoom room = studyRoomRepository.findByRoomIdAndStatus(roomId, RoomStatType.OPEN)
                .orElseThrow(() -> new BaseException(BaseErrorCode.ROOM_NOT_FOUND_ERROR));

        // [Fix] Ghost Session Logic: 기존 세션이 있다면 강제 종료 후 재입장 허용
        studySessionRepository.findByMemberAndEndTimeIsNull(member).ifPresent(StudySession::close);

        int currentUsers = studySessionRepository.findByRoomAndEndTimeIsNull(room).size();
        if (currentUsers >= MAX_USER) {
            throw new BaseException(BaseErrorCode.ROOM_FULL_ERROR);
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
        token.setIdentity(member.getMemberId());
        token.addGrants(new RoomJoin(true), new RoomName(roomId));

        return new SessionMemberInfoResponse(member, token.toJwt());
    }

    public SessionMemberInfoResponse enterRandomRoom(Member member) {
        List<StudyRoom> openRooms = studyRoomRepository.findAllByStatus(RoomStatType.OPEN);
        if (openRooms.isEmpty()) {
            throw new BaseException(BaseErrorCode.ROOM_NOT_AVAILABLE_ERROR);
        }

        List<StudyRoom> availableRooms = openRooms.stream()
                .filter(room -> studySessionRepository.findByRoomAndEndTimeIsNull(room).size() < MAX_USER)
                .toList();

        if (availableRooms.isEmpty()) {
            throw new BaseException(BaseErrorCode.ROOM_NOT_AVAILABLE_ERROR);
        }

        // TODO : 초개인화 방향으로 RANDOM 로직 수정
        StudyRoom randomRoom = availableRooms.get(ThreadLocalRandom.current().nextInt(availableRooms.size()));

        return enterRoom(member, randomRoom.getRoomId());
    }

    public void exitRoom(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR));

        StudySession session = studySessionRepository.findByMemberAndEndTimeIsNull(member)
                .orElseThrow(() -> new BaseException(BaseErrorCode.ROOM_NOT_PARTICIPATE_ERROR));

        session.close();
    }
}
