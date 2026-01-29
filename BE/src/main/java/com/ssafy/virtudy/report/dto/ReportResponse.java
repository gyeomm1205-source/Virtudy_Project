package com.ssafy.virtudy.report.dto;

import com.ssafy.virtudy.report.domain.Report;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReportResponse {
    private String reportId; // UUID (외부 공개용)
    // private LocalDate reportDate;
    private int focusDepthPercentage; // 집중도 (백분위)
    private int totalStudyTime; // 총 공부시간 
    private int endurance; // 지구력 
    private int focusDepth; // 집중력 
    private int regularity; // 규칙성
    private int stability; // 안정성
    private int willPower; // 의지력 
    private String aiComment; // AI 코멘트

    // Entity -> DTO 변환 (정적 팩토리 메서드 패턴 추천)
    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .reportId(report.getReportId())
                // .reportDate(report.getReportDate())
                .focusDepthPercentage(report.getFocusDepthPercentage())
                .totalStudyTime(report.getTotalStudyTime())
                .endurance(report.getEndurance())
                .focusDepth(report.getFocusDepth())
                .regularity(report.getRegularity())
                .stability(report.getStability())
                .willPower(report.getWillPower())
                .aiComment(report.getAiComment())
                .build();
    }
}
