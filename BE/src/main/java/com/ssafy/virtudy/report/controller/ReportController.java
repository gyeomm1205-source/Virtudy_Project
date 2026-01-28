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
     * 주간 리포트 조회 API
     * 특정 기간(Start ~ End) 동안 생성된 리포트 목록을 조회합니다.
     *
     * @param userPrincipal 로그인한 사용자 정보
     * @param startDate     조회 시작일
     * @param endDate       조회 종료일
     * @return List<ReportResponse> 해당 기간의 주간 리포트 리스트
     */
    @Operation(summary = "주간 리포트 조회", description = "특정 기간 동안 생성된 주간 학습 리포트 목록을 조회합니다.")
    @GetMapping("/weekly")
    public ResponseEntity<List<ReportResponse>> getWeeklyReport(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        
        List<ReportResponse> response = reportService.getWeeklyReport(userPrincipal.getUsername(), startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
