package com.ssafy.virtudy.study.domain;

import com.ssafy.virtudy.common.BaseTimeEntity;
import com.ssafy.virtudy.member.domain.Member;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class StudySession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // mysql의 auto_inc 사용하기 위함
    private Long id; // PK

    @Column(nullable = false)
    private String sessionId; // UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Member member; // FK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ROOM_ID")
    private StudyRoom room; // FK

    @Column(nullable = false, updatable = false)
    private LocalDateTime startTime;    // 세션 시작시각

    @Column(nullable = false, updatable = false)
    private LocalDateTime endTime;      // 세션 종료시각
}