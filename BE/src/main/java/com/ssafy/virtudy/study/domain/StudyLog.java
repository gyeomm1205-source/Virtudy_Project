package com.ssafy.virtudy.study.domain;

import com.ssafy.virtudy.member.domain.Member;
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
    private StudySession session; // FK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Member member; // FK

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyEventType eventType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime detectedAt;
}
