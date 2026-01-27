package com.ssafy.virtudy.group.repository;

import com.ssafy.virtudy.group.domain.RoomMember;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
=======
>>>>>>> e788f78 ([S14P11A703-136] 엔티티 최신화)
=======
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
>>>>>>> 6e9e953 ([S14P11A703-106] API 명세서 구체화)

import java.util.List;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    int countByRoom(StudyRoom room);
<<<<<<< HEAD
<<<<<<< HEAD
    boolean existsByMemberAndRoom(Member member, StudyRoom room);

    @Query(value = "SELECT rm.* FROM room_member rm " +
            "JOIN study_room sr ON rm.room_id = sr.id " +
            "WHERE rm.member_id = :memberId AND sr.status = 'OPEN'", nativeQuery = true)
    List<RoomMember> findAllByMemberAndRoomStatusOpen(@Param("memberId") Long memberId);
=======
    List<RoomMember> findAllByMember(Member member);
    boolean existsByMemberAndRoom(Member member, StudyRoom room);
>>>>>>> e788f78 ([S14P11A703-136] 엔티티 최신화)
=======
    boolean existsByMemberAndRoom(Member member, StudyRoom room);

    @Query(value = "SELECT rm.* FROM room_member rm " +
            "JOIN study_room sr ON rm.room_id = sr.id " +
            "WHERE rm.member_id = :memberId AND sr.status = 'OPEN'", nativeQuery = true)
    List<RoomMember> findAllByMemberAndRoomStatusOpen(@Param("memberId") Long memberId);
>>>>>>> 6e9e953 ([S14P11A703-106] API 명세서 구체화)
}
