package com.ssafy.virtudy.study.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class StudyLog {

    @Id
    @GeneratedValue
    private Long id; // PK

    @Column(nullable = false)
    private String logId; // UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SESSION_ID")
    private Long sessionId; // FK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Long memberId; // FK

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyEventType eventType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime detectedAt;
}
