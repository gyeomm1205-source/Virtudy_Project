package com.ssafy.virtudy.study.repository;

import com.ssafy.virtudy.study.domain.StudyLog;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
=======
>>>>>>> c0ec20e ([S14P11A703-138] 스터디 로그 C)
import org.springframework.stereotype.Repository;

@Repository
public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {
<<<<<<< HEAD

    /**
     * 특정 세션에 속한 모든 학습 로그를 조회합니다.
     * @param sessionId 세션의 PK
     * @return 해당 세션의 로그 리스트
     */
    List<StudyLog> findBySessionId(Long sessionId);

    /**
     * 특정 멤버의 특정 기간 동안의 로그를 조회합니다.
     * (리포트 생성이나 통계 분석 시 사용 가능)
     *
     * @param memberId 멤버의 PK
     * @param start 조회 시작 시간
     * @param end 조회 종료 시간
     * @return 해당 기간 내의 로그 리스트
     */
    @Query("SELECT sl FROM StudyLog sl WHERE sl.member.id = :memberId AND sl.detectedAt BETWEEN :start AND :end")
    List<StudyLog> findByMemberIdAndDateRange(@Param("memberId") Long memberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
=======
>>>>>>> c0ec20e ([S14P11A703-138] 스터디 로그 C)
}
