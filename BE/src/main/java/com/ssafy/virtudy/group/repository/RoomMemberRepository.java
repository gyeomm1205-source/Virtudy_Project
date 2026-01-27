package com.ssafy.virtudy.group.repository;

import com.ssafy.virtudy.group.domain.RoomMember;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    int countByRoom(StudyRoom room);
    boolean existsByMemberAndRoom(Member member, StudyRoom room);

    @Query(value = "SELECT rm.* FROM room_member rm " +
            "JOIN study_room sr ON rm.room_id = sr.id " +
            "WHERE rm.member_id = :memberId AND sr.status = 'OPEN'", nativeQuery = true)
    List<RoomMember> findAllByMemberAndRoomStatusOpen(@Param("memberId") Long memberId);
}
