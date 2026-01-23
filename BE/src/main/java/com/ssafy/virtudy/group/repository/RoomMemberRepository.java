package com.ssafy.virtudy.group.repository;

import com.ssafy.virtudy.group.domain.RoomMember;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    int countByRoom(StudyRoom room);
    List<RoomMember> findAllByMember(Member member);
    boolean existsByMemberAndRoom(Member member, StudyRoom room);
}
