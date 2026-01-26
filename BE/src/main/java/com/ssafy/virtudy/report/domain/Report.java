package com.ssafy.virtudy.report.domain;

import com.ssafy.virtudy.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 필수: 기본 생성자
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // mysql의 auto_inc 사용하기 위함
    private Long id; // PK

    @Column(nullable = false)
    private String reportId; // UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Member member; // FK

    @Column(nullable = false, updatable = false)
    private LocalDate reportDate;

    @Column(nullable = false)
    private int endurance = 0; // 지구력

    @Column(nullable = false)
    private int focusDepth = 0; // 집중도

    @Column(nullable = false)
    private int regularity = 0; // 규칙성

    @Column(nullable = false)
    private int stability = 0; // 안정감

    @Column(nullable = false)
    private int willPower = 0; // 의지력

    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiComment;

    @Column(nullable = false)
    private int maxFocusTime = 0; // 최대 집중 시간 (분)

    @Column
    private String sleepVulnerableTime; // 졸음 취약 시간대 (예: "14:00", "14")

    @Column
    private String distractionPatternTime; // 딴짓(폰/이탈) 빈번 시간대

    @Builder
    public Report(Member member, LocalDate reportDate,
                  int endurance, int focusDepth, int regularity,
                  int stability, int willPower, String aiComment,
                  int maxFocusTime, String sleepVulnerableTime, String distractionPatternTime) {
        this.reportId = UUID.randomUUID().toString();
        this.member = member;
        this.reportDate = reportDate;
        this.endurance = endurance;
        this.focusDepth = focusDepth;
        this.regularity = regularity;
        this.stability = stability;
        this.willPower = willPower;
        this.aiComment = aiComment;
        this.maxFocusTime = maxFocusTime;
        this.sleepVulnerableTime = sleepVulnerableTime;
        this.distractionPatternTime = distractionPatternTime;
    }
}
