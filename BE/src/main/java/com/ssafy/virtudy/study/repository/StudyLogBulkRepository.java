package com.ssafy.virtudy.study.repository;

import com.ssafy.virtudy.study.domain.StudyLog;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StudyLogBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void saveAllBatch(List<StudyLog> logs) {
        if (logs.isEmpty()) return;

        // 1. 실행할 SQL 준비 (ID는 Auto Increment라서 생략)
        String sql = "INSERT INTO study_log (log_id, session_id, member_id, event_type, detected_at) " +
                     "VALUES (?, ?, ?, ?, ?)";

        // 2. JDBC 배치 실행
        jdbcTemplate.batchUpdate(sql,
            logs,
            500, // 배치 사이즈 (Kafka랑 맞추거나 적절히 조절)
            (PreparedStatement ps, StudyLog log) -> {
                ps.setString(1, log.getLogId()); // UUID
                ps.setLong(2, log.getSession().getId()); // FK (Session ID)
                ps.setLong(3, log.getMember().getId());  // FK (Member ID)
                ps.setString(4, log.getEventType().name()); // Enum -> String
                ps.setObject(5, log.getDetectedAt()); // LocalDateTime
            });
    }
}
