package com.ssafy.virtudy.report.repository;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    // Read 1: 특정 회원의 모든 리포트 조회 (최신순 정렬 등 필요 가능성 있음)
    // 현재는 사용되지 않으나 추후 전체 기록 조회 시 사용 가능
    List<Report> findByMember(Member member);

    // Read 2: 외부 노출용 UUID로 단건 조회
    // 보안상 PK(Long id) 대신 UUID(reportId)를 사용하여 리포트를 조회할 때 사용
    Optional<Report> findByReportId(String reportId);

    /**
     * 특정 회원의 특정 날짜 리포트 존재 여부 확인
     * 중복 리포트 생성 방지를 위해 배치(Batch) 로직에서 사용됩니다.
     */
    boolean existsByMemberAndReportDate(Member member, LocalDate reportDate);

    /**
     * 특정 회원의 특정 날짜 리포트 단건 조회
     * 일간 리포트 API (/api/report/daily)에서 사용됩니다.
     */
    Optional<Report> findByMemberAndReportDate(Member member, LocalDate reportDate);

    /**
     * 특정 기간 내의 리포트 목록 조회
     * 주간 리포트 API (/api/report/weekly)에서 사용되며, 시작일과 종료일 사이의 모든 리포트를 반환합니다.
     */
    List<Report> findByMemberAndReportDateBetween(Member member, LocalDate startDate, LocalDate endDate);
}
