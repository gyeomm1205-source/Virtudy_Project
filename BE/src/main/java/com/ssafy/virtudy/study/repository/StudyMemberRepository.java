package com.ssafy.virtudy.study.repository;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.StudyMember;
import com.ssafy.virtudy.study.domain.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    int countByStudyRoom(StudyRoom studyRoom);
    List<StudyMember> findAllByMember(Member member);
    boolean existsByMemberAndStudyRoom(Member member, StudyRoom studyRoom);
}
