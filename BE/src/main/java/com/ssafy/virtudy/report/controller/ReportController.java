package com.ssafy.virtudy.report.controller;

import com.ssafy.virtudy.global.auth.principal.UserPrincipal;
import com.ssafy.virtudy.report.dto.ReportResponse;
import com.ssafy.virtudy.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Report API", description = "학습 리포트 관련 API")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 일간 리포트 조회 API
     * 클라이언트가 요청한 날짜(date)에 해당하는 일간 리포트 데이터를 반환합니다.
     * 
     * @param userPrincipal 로그인한 사용자 정보 (Spring Security 주입)
     * @param date          조회하고자 하는 날짜 (QueryString: ?date=2024-01-25)
     * @return ReportResponse 리포트 객체 (없을 경우 404 에러)
     */
    @Operation(summary = "일간 리포트 조회", description = "특정 날짜의 학습 리포트(지구력, 집중력, AI 코멘트 등)를 조회합니다.")
    @GetMapping("/daily")
    public ResponseEntity<ReportResponse> getDailyReport(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam LocalDate date) {
        
        // Service 호출 시 UserPrincipal.getUsername() (= MemberId PK 아님, 식별자) 사용
        ReportResponse response = reportService.getDailyReport(userPrincipal.getUsername(), date);
        return ResponseEntity.ok(response);
    }

    /**
     * 주간 리포트 조회 API
     * 특정 기간(Start ~ End) 동안의 리포트 목록을 조회합니다.
     * 주로 캘린더 뷰나 주간 통계 그래프를 그릴 때 사용됩니다.
     *
     * @param userPrincipal 로그인한 사용자 정보
     * @param startDate     조회 시작일
     * @param endDate       조회 종료일
     * @return List<ReportResponse> 해당 기간의 리포트 리스트
     */
    @Operation(summary = "주간 리포트 조회", description = "특정 기간(예: 7일) 동안 생성된 모든 학습 리포트 목록을 조회합니다.")
    @GetMapping("/weekly")
    public ResponseEntity<List<ReportResponse>> getWeeklyReport(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        
        List<ReportResponse> response = reportService.getWeeklyReport(userPrincipal.getUsername(), startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
