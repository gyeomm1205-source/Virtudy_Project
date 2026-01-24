package com.ssafy.virtudy.study.repository;

import com.ssafy.virtudy.study.domain.StudyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {
}
