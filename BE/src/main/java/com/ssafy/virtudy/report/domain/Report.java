package com.ssafy.virtudy.report.domain;

import com.ssafy.virtudy.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
    
    // TODO 집중도는 퍼센트로 계산되고 있음. 
    // 집중력 기반으로 백분위 계산하면 됨

    @Column(nullable = false)
    private LocalDate reportDate; // 리포트 기준 날짜 (그 주의 월요일)

    @Column(nullable = false)
    private int focusDepthPercentage = 0; // 집중도 (백분위)

    @Column(nullable = false)
<<<<<<< HEAD
    private int avgStudyTime = 0; // 평균 공부시간 
=======
    private int totalStudyTime = 0; // 총 공부시간
>>>>>>> 317f96e202cdb0fc59fa575fb5cd7806f9f6905d

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
<<<<<<< HEAD
    public Report(Member member, LocalDate reportDate, int focusDepthPercentage, int avgStudyTime,
=======
    public Report(Member member, LocalDate reportDate, int focusDepthPercentage, int totalStudyTime,
>>>>>>> 317f96e202cdb0fc59fa575fb5cd7806f9f6905d
                  int endurance, int focusDepth, int regularity,
                  int stability, int willPower, String aiComment,
                  int maxFocusTime, String sleepVulnerableTime, String distractionPatternTime) {
        this.reportId = UUID.randomUUID().toString();
        this.member = member;
        this.reportDate = reportDate;
        this.focusDepthPercentage = focusDepthPercentage;
<<<<<<< HEAD
        this.avgStudyTime = avgStudyTime;
=======
        this.totalStudyTime = totalStudyTime;
>>>>>>> 317f96e202cdb0fc59fa575fb5cd7806f9f6905d
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
