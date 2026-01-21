package com.ssafy.virtudy.study.repository;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.StudyRoom;
import com.ssafy.virtudy.study.domain.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    // 특정 방에서 아직 종료되지 않은(활성) 세션 목록 조회
    List<StudySession> findByRoomAndEndTimeIsNull(StudyRoom studyRoom);

    // 특정 사용자의 활성 세션 조회
    Optional<StudySession> findByMemberAndEndTimeIsNull(Member member);

    // 특정 사용자의 특정 방에 대한 가장 최신 세션 조회
    Optional<StudySession> findTopByMemberAndRoomOrderByStartTimeDesc(Member member, StudyRoom studyRoom);
}
