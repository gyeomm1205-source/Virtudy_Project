package com.ssafy.virtudy.report.dto;

import com.ssafy.virtudy.report.domain.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class ReportResponse {
    private String reportId; // UUID (외부 공개용)
    private LocalDate reportDate;
    private int endurance;
    private int focusDepth;
    private int regularity;
    private int stability;
    private int willPower;
    private String aiComment;

    // Entity -> DTO 변환 (정적 팩토리 메서드 패턴 추천)
    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .reportId(report.getReportId())
                .reportDate(report.getReportDate())
                .endurance(report.getEndurance())
                .focusDepth(report.getFocusDepth())
                .regularity(report.getRegularity())
                .stability(report.getStability())
                .willPower(report.getWillPower())
                .aiComment(report.getAiComment())
                .build();
    }
}
