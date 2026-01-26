package com.ssafy.virtudy.study.service;

import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
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
    private final RoomMemberRepository roomMemberRepository;

    @Transactional
    public StudyRoomResponse createRoom(Member owner, StudyRoomSaveRequest request) {
        if (studyRoomRepository.countByOwnerIdAndStatus(owner.getId(), RoomStatType.OPEN) >= 3) {
            throw new BaseException(BaseErrorCode.ROOM_MAX_REACHED_ERROR);
        }

        boolean hasPassword = request.getPassword() != null && !request.getPassword().isBlank();

        if (request.getType() == RoomType.PRIVATE && !hasPassword) {
            throw new BaseException(BaseErrorCode.ROOM_PRIVATE_EMPTY_PASSWORD_ERROR);
        }

        if (request.getType() == RoomType.PUBLIC && hasPassword) {
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

    public List<StudyRoomListResponse> findMyRooms(Member member) {
        List<StudyRoom> ownedRooms = studyRoomRepository.findAllByOwnerId(member.getId());
        List<StudyRoom> joinedRooms = roomMemberRepository.findAllByMember(member).stream()
                .map(RoomMember::getRoom)
                .toList();

        // TODO : 이렇게 만들면 추가 쿼리가 10번 나간다..
        return Stream.concat(ownedRooms.stream(), joinedRooms.stream())
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
                .orElseThrow(() -> new BaseException(BaseErrorCode.ROOM_NOT_FOUND_ERROR));
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
            throw new BaseException(BaseErrorCode.ROOM_NOT_PARTICIPATE_ERROR);
        }

        member.setFavoriteRoom(studyRoom);
    }
}
