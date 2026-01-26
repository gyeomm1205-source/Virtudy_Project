package com.ssafy.virtudy.study.service;

<<<<<<< HEAD
<<<<<<< HEAD
import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
=======
>>>>>>> e788f78 ([S14P11A703-136] 엔티티 최신화)
=======
import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
>>>>>>> 7e431b7 ([S14P11A703-105] API에 사용자 로직 추가 및 사용자 정의 Error Code 작성)
import com.ssafy.virtudy.group.domain.RoomMember;
import com.ssafy.virtudy.group.repository.RoomMemberRepository;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.RoomStatType;
import com.ssafy.virtudy.study.domain.RoomType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import com.ssafy.virtudy.study.dto.StudyRoomListResponse;
import com.ssafy.virtudy.study.dto.StudyRoomResponse;
import com.ssafy.virtudy.study.dto.StudyRoomSaveRequest;
import com.ssafy.virtudy.study.dto.StudyRoomUpdateRequest;
import com.ssafy.virtudy.study.repository.StudyRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyRoomService {

    private final StudyRoomRepository studyRoomRepository;
<<<<<<< HEAD
<<<<<<< HEAD
=======
    private final MemberRepository memberRepository;
>>>>>>> e788f78 ([S14P11A703-136] 엔티티 최신화)
=======
>>>>>>> 7e431b7 ([S14P11A703-105] API에 사용자 로직 추가 및 사용자 정의 Error Code 작성)
    private final RoomMemberRepository roomMemberRepository;

    @Transactional
    public StudyRoomResponse createRoom(Member owner, StudyRoomSaveRequest request) {
        if (studyRoomRepository.countByOwnerIdAndStatus(owner.getId(), RoomStatType.OPEN) >= 3) {
            throw new BaseException(BaseErrorCode.ROOM_MAX_REACHED_ERROR);
        }

        boolean hasPassword = request.getPassword() != null && !request.getPassword().isBlank();

<<<<<<< HEAD
        if (RoomType.valueOf(request.getType()) == RoomType.PRIVATE && !hasPassword) {
            throw new BaseException(BaseErrorCode.ROOM_PRIVATE_EMPTY_PASSWORD_ERROR);
        }

        if (RoomType.valueOf(request.getType()) == RoomType.PUBLIC && hasPassword) {
=======
        if (request.getType() == RoomType.PRIVATE && !hasPassword) {
            throw new BaseException(BaseErrorCode.ROOM_PRIVATE_EMPTY_PASSWORD_ERROR);
        }

        if (request.getType() == RoomType.PUBLIC && hasPassword) {
>>>>>>> 7e431b7 ([S14P11A703-105] API에 사용자 로직 추가 및 사용자 정의 Error Code 작성)
            throw new BaseException(BaseErrorCode.ROOM_PUBLIC_FILLED_PASSWORD_ERROR);
        }

        StudyRoom studyRoom = studyRoomRepository.save(request.toEntity(owner));
        roomMemberRepository.save(RoomMember.builder()
                .roomMemberId(UUID.randomUUID().toString())
                .room(studyRoom)
                .member(owner)
                .joinedAt(LocalDateTime.now())
                .build());
        return new StudyRoomResponse(studyRoom, 1);
    }

    public List<StudyRoomListResponse> findAllOpenRooms() {
        return studyRoomRepository.findAllByStatus(RoomStatType.OPEN).stream()
                .map(studyRoom -> {
                    int currentUser = roomMemberRepository.countByRoom(studyRoom);
                    return new StudyRoomListResponse(studyRoom, currentUser);
                })
                .collect(Collectors.toList());
    }

<<<<<<< HEAD
<<<<<<< HEAD
    public List<StudyRoomListResponse> findMyRooms(Member member) {
        List<StudyRoom> joinedRooms = roomMemberRepository.findAllByMemberAndRoomStatusOpen(member.getId()).stream()
=======
    public List<StudyRoomListResponse> findMyRooms(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

=======
    public List<StudyRoomListResponse> findMyRooms(Member member) {
>>>>>>> 7e431b7 ([S14P11A703-105] API에 사용자 로직 추가 및 사용자 정의 Error Code 작성)
        List<StudyRoom> ownedRooms = studyRoomRepository.findAllByOwnerId(member.getId());
        List<StudyRoom> joinedRooms = roomMemberRepository.findAllByMember(member).stream()
>>>>>>> e788f78 ([S14P11A703-136] 엔티티 최신화)
                .map(RoomMember::getRoom)
                .toList();

        // TODO : 이렇게 만들면 추가 쿼리가 10번 나간다..
        return joinedRooms.stream()
                .distinct()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(10)
                .map(studyRoom -> {
                    int currentUser = roomMemberRepository.countByRoom(studyRoom);
                    return new StudyRoomListResponse(studyRoom, currentUser);
                })
                .collect(Collectors.toList());
    }

    public StudyRoomResponse findRoomByCode(String roomId) {
        StudyRoom studyRoom = studyRoomRepository.findByRoomIdAndStatus(roomId, RoomStatType.OPEN)
<<<<<<< HEAD
<<<<<<< HEAD
                .orElseThrow(() -> new BaseException(BaseErrorCode.ROOM_NOT_FOUND_ERROR));
=======
                .orElseThrow(() -> new IllegalArgumentException("종료되었거나 존재하지 않는 방입니다."));
>>>>>>> e788f78 ([S14P11A703-136] 엔티티 최신화)
=======
                .orElseThrow(() -> new BaseException(BaseErrorCode.ROOM_NOT_FOUND_ERROR));
>>>>>>> 7e431b7 ([S14P11A703-105] API에 사용자 로직 추가 및 사용자 정의 Error Code 작성)
        int currentUser = roomMemberRepository.countByRoom(studyRoom);
        return new StudyRoomResponse(studyRoom, currentUser);
    }

    @Transactional
    public void updateRoom(Member member, String roomId, StudyRoomUpdateRequest request) {
        StudyRoom studyRoom = studyRoomRepository.findByRoomIdAndStatus(roomId, RoomStatType.OPEN)
                .orElseThrow(() -> new BaseException(BaseErrorCode.ROOM_NOT_FOUND_ERROR));

        if (!studyRoom.getOwner().getId().equals(member.getId())) {
            throw new BaseException(BaseErrorCode.ROOM_NOT_OWNER_ERROR);
        }

        studyRoom.update(request.getTitle(), request.getPassword(), request.getDescription());
    }

    @Transactional
    public void deleteRoom(Member member, String roomId) {
        StudyRoom studyRoom = studyRoomRepository.findByRoomIdAndStatus(roomId, RoomStatType.OPEN)
                .orElseThrow(() -> new BaseException(BaseErrorCode.ROOM_NOT_FOUND_ERROR));

        if (!studyRoom.getOwner().getId().equals(member.getId())) {
            throw new BaseException(BaseErrorCode.ROOM_NOT_OWNER_ERROR);
        }

        studyRoom.close();
    }

    @Transactional
    public void setFavoriteRoom(Member member, String roomId) {
        StudyRoom studyRoom = studyRoomRepository.findByRoomIdAndStatus(roomId, RoomStatType.OPEN)
                .orElseThrow(() -> new BaseException(BaseErrorCode.ROOM_NOT_FOUND_ERROR));

        if (!roomMemberRepository.existsByMemberAndRoom(member, studyRoom)) {
<<<<<<< HEAD
<<<<<<< HEAD
            throw new BaseException(BaseErrorCode.ROOM_NOT_PARTICIPATE_ERROR);
=======
            throw new IllegalStateException("방에 참여하고 있지 않습니다.");
>>>>>>> e788f78 ([S14P11A703-136] 엔티티 최신화)
=======
            throw new BaseException(BaseErrorCode.ROOM_NOT_PARTICIPATE_ERROR);
>>>>>>> 7e431b7 ([S14P11A703-105] API에 사용자 로직 추가 및 사용자 정의 Error Code 작성)
        }

        member.setFavoriteRoom(studyRoom);
    }
}
