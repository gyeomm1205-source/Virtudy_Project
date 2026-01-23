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
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyRoomService {

    private final StudyRoomRepository studyRoomRepository;
    private final RoomMemberRepository roomMemberRepository;

    private final static int MAX_CREATE = 3;
    private final static int MAX_USER = 6;
    private final static int MY_ROOM_LIST_SIZE = 10;


    @Transactional
    public StudyRoomResponse createRoom(Member owner, StudyRoomSaveRequest request) {
        if (studyRoomRepository.countByOwnerIdAndStatus(owner.getId(), RoomStatType.OPEN) >= MAX_CREATE) {
            throw new BaseException(BaseErrorCode.ROOM_MAX_REACHED_ERROR);
        }

        boolean hasPassword = request.getPassword() != null && !request.getPassword().isBlank();

        if (RoomType.valueOf(request.getType()) == RoomType.PRIVATE && !hasPassword) {
            throw new BaseException(BaseErrorCode.ROOM_PRIVATE_EMPTY_PASSWORD_ERROR);
        }

        if (RoomType.valueOf(request.getType()) == RoomType.PUBLIC && hasPassword) {
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

    public List<StudyRoomListResponse> findAllOpenRooms(Member member) {
        return studyRoomRepository.findAllByStatus(RoomStatType.OPEN).stream()
                .map(room -> new AbstractMap.SimpleEntry<>(room, roomMemberRepository.countByRoom(room)))
                .sorted((e1, e2) -> {
                    StudyRoom r1 = e1.getKey();
                    int c1 = e1.getValue();
                    StudyRoom r2 = e2.getKey();
                    int c2 = e2.getValue();

                    boolean f1 = c1 >= MAX_USER;
                    boolean f2 = c2 >= MAX_USER;

                    if (f1 != f2) return f1 ? 1 : -1;

                    if (c1 != c2) return Integer.compare(c2, c1);

                    return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                })
                .map(e -> {
                    boolean isOwner = e.getKey().getOwner().getId().equals(member.getId());
                    return new StudyRoomListResponse(e.getKey(), e.getValue(), isOwner);
                })
                .collect(Collectors.toList());
    }

    public List<StudyRoomListResponse> findMyRooms(Member member) {
        List<RoomMember> roomMembers = roomMemberRepository.findAllByMemberAndRoomStatusOpen(member.getId());

        Map<Long, LocalDateTime> joinedAtMap = roomMembers.stream()
                .collect(Collectors.toMap(
                        rm -> rm.getRoom().getId(),
                        RoomMember::getJoinedAt,
                        (existing, replacement) -> existing.isAfter(replacement) ? existing : replacement
                ));

        List<StudyRoom> joinedRooms = roomMembers.stream()
                .map(RoomMember::getRoom)
                .distinct()
                .toList();

        Long favoriteRoomId = (member.getFavoriteRoom() != null) ? member.getFavoriteRoom().getId() : null;

        return joinedRooms.stream()
                .sorted((r1, r2) -> {
                    boolean r1Owner = r1.getOwner().getId().equals(member.getId());
                    boolean r2Owner = r2.getOwner().getId().equals(member.getId());
                    boolean r1Fav = r1.getId().equals(favoriteRoomId);
                    boolean r2Fav = r2.getId().equals(favoriteRoomId);

                    int p1 = getPriority(r1Owner, r1Fav);
                    int p2 = getPriority(r2Owner, r2Fav);

                    if (p1 != p2) return Integer.compare(p1, p2);

                    LocalDateTime j1 = joinedAtMap.get(r1.getId());
                    LocalDateTime j2 = joinedAtMap.get(r2.getId());
                    return j2.compareTo(j1);
                })
                .limit(MY_ROOM_LIST_SIZE)
                .map(studyRoom -> {
                    int currentUser = roomMemberRepository.countByRoom(studyRoom);
                    boolean isOwner = studyRoom.getOwner().getId().equals(member.getId());
                    return new StudyRoomListResponse(studyRoom, currentUser, isOwner);
                })
                .collect(Collectors.toList());
    }

    private int getPriority(boolean isOwner, boolean isFavorite) {
        if (isOwner && isFavorite) return 1;
        if (isOwner) return 2;
        if (isFavorite) return 3;
        return 4;
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