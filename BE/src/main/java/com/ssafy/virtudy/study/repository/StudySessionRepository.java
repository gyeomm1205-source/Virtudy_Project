package com.ssafy.virtudy.study.repository;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.StudyRoom;
import com.ssafy.virtudy.study.domain.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    // 특정 방에서 아직 종료되지 않은(활성) 세션 목록 조회
    List<StudySession> findByRoomAndEndTimeIsNull(StudyRoom studyRoom);

    // 특정 방에서 아직 종료되지 않은(활성) 세션 수 조회
    int countByRoomAndEndTimeIsNull(StudyRoom studyRoom);

    // 특정 사용자의 활성 세션 조회
    Optional<StudySession> findByMemberAndEndTimeIsNull(Member member);

    // 특정 사용자의 특정 방에 대한 가장 최신 세션 조회
    Optional<StudySession> findTopByMemberAndRoomOrderByStartTimeDesc(Member member, StudyRoom studyRoom);

    // 세션 ID로 세션 조회
    Optional<StudySession> findBySessionId(String sessionId);

    // 특정 기간 동안 종료된 세션 조회 (티어 스코어 갱신용)
    @Query("SELECT ss FROM StudySession ss WHERE ss.endTime BETWEEN :start AND :end")
    List<StudySession> findByEndTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<StudySession> findAllByMemberAndStartTimeBetween(Member member, LocalDateTime start, LocalDateTime end);
}