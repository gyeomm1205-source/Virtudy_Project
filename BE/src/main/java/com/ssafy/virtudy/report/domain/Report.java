package com.ssafy.virtudy.report.domain;

import com.ssafy.virtudy.member.domain.Member;
import jakarta.persistence.*;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
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

    @Column(nullable = false)
    private String aiComment;
}
