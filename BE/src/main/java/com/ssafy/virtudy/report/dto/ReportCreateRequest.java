package com.ssafy.virtudy.report.dto;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.report.domain.Report;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ReportCreateRequest {
    @Min(0) @Max(100)
    private int endurance;

    private int focusDepth;

    private int regularity;

    private int stability;

    private int willPower;

    private String aiComment;

    // DTO -> Entity 변환 메서드 (Service에서 사용)
    public Report toEntity(Member member){
        return Report.builder()
                .member(member)
                .reportDate(LocalDate.now())
                .endurance(this.endurance)
                .focusDepth(this.focusDepth)
                .regularity(this.regularity)
                .stability(this.stability)
                .willPower(this.willPower)
                .aiComment(this.aiComment)
                .build();
    }


}
