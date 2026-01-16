package com.ssafy.virtudy.study.domain;

import com.ssafy.virtudy.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class StudySession extends BaseTimeEntity {

    @Id
    @GeneratedValue
    private Long id; // PK

    @Column(nullable = false)
    private String sessionId; // UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Long memberId; // FK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ROOM_ID")
    private Long roomId; // FK

    @Column(nullable = false, updatable = false)
    private LocalDateTime startTime;    // 세션 시작시각

    @Column(nullable = false, updatable = false)
    private LocalDateTime endTime;      // 세션 종료시각
}