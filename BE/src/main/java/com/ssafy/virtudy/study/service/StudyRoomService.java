package com.ssafy.virtudy.study.service;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.repository.MemberRepository;
import com.ssafy.virtudy.study.domain.RoomStatType;
import com.ssafy.virtudy.study.domain.RoomType;
import com.ssafy.virtudy.study.domain.StudyMember;
import com.ssafy.virtudy.study.domain.StudyRoom;
import com.ssafy.virtudy.study.dto.StudyRoomListResponse;
import com.ssafy.virtudy.study.dto.StudyRoomResponse;
import com.ssafy.virtudy.study.dto.StudyRoomSaveRequest;
import com.ssafy.virtudy.study.dto.StudyRoomUpdateRequest;
import com.ssafy.virtudy.study.repository.StudyMemberRepository;
import com.ssafy.virtudy.study.repository.StudyRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * TODO : Throw 형식 수정
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyRoomService {

    private final StudyRoomRepository studyRoomRepository;
    private final MemberRepository memberRepository;
    private final StudyMemberRepository studyMemberRepository;

    @Transactional
    public StudyRoomResponse createRoom(String memberId, StudyRoomSaveRequest request) {
        Member owner = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

        if (studyRoomRepository.countByOwnerIdAndStatus(owner.getId(), RoomStatType.OPEN) >= 3) {
            throw new IllegalStateException("방은 최대 3개까지 생성할 수 있습니다.");
        }

        boolean hasPassword = request.getPassword() != null && !request.getPassword().isBlank();

        if (request.getType() == RoomType.PRIVATE && !hasPassword) {
            throw new IllegalArgumentException("비공개 방은 비밀번호가 필수입니다.");
        }

        if (request.getType() == RoomType.PUBLIC && hasPassword) {
            throw new IllegalArgumentException("공개 방은 비밀번호가 존재하지 않아야 합니다.");
        }

        StudyRoom studyRoom = studyRoomRepository.save(request.toEntity(owner));
        studyMemberRepository.save(StudyMember.of(owner, studyRoom));
        return new StudyRoomResponse(studyRoom, 1);
    }

    public List<StudyRoomListResponse> findAllOpenRooms() {
        return studyRoomRepository.findAllByStatus(RoomStatType.OPEN).stream()
                .map(studyRoom -> {
                    int currentUser = studyMemberRepository.countByStudyRoom(studyRoom);
                    return new StudyRoomListResponse(studyRoom, currentUser);
                })
                .collect(Collectors.toList());
    }

    public List<StudyRoomListResponse> findMyRooms(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

        List<StudyRoom> ownedRooms = studyRoomRepository.findAllByOwnerId(member.getId());
        List<StudyRoom> joinedRooms = studyMemberRepository.findAllByMember(member).stream()
                .map(StudyMember::getStudyRoom)
                .toList();

        // TODO : 이렇게 만들면 추가 쿼리가 10번 나간다..
        return Stream.concat(ownedRooms.stream(), joinedRooms.stream())
                .distinct()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(10)
                .map(studyRoom -> {
                    int currentUser = studyMemberRepository.countByStudyRoom(studyRoom);
                    return new StudyRoomListResponse(studyRoom, currentUser);
                })
                .collect(Collectors.toList());
    }

    public StudyRoomResponse findRoomByCode(String roomId) {
        StudyRoom studyRoom = studyRoomRepository.findByRoomIdAndStatus(roomId, RoomStatType.OPEN)
                .orElseThrow(() -> new IllegalArgumentException("종료되었거나 존재하지 않는 방입니다."));
        int currentUser = studyMemberRepository.countByStudyRoom(studyRoom);
        return new StudyRoomResponse(studyRoom, currentUser);
    }

    @Transactional
    public void updateRoom(String memberId, String roomId, StudyRoomUpdateRequest request) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
        StudyRoom studyRoom = studyRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        if (studyRoom.getStatus() == RoomStatType.CLOSED) {
            throw new IllegalStateException("종료된 방입니다.");
        }

        if (!studyRoom.getOwner().getId().equals(member.getId())) {
            throw new IllegalStateException("방장만 수정할 수 있습니다.");
        }

        studyRoom.update(request.getTitle(), request.getPassword());
    }

    @Transactional
    public void deleteRoom(String memberId, String roomId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
        StudyRoom studyRoom = studyRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        if (studyRoom.getStatus() == RoomStatType.CLOSED) {
            throw new IllegalStateException("이미 종료된 방입니다.");
        }

        if (!studyRoom.getOwner().getId().equals(member.getId())) {
            throw new IllegalStateException("방장만 삭제할 수 있습니다.");
        }

        studyRoom.close();
    }

    @Transactional
    public void setFavoriteRoom(String memberId, String roomId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
        StudyRoom studyRoom = studyRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        if (!studyMemberRepository.existsByMemberAndStudyRoom(member, studyRoom)) {
            throw new IllegalStateException("방에 참여하고 있지 않습니다.");
        }

        member.setFavoriteRoom(studyRoom);
    }
}
