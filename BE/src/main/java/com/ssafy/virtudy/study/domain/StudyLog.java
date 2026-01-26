package com.ssafy.virtudy.study.domain;

import com.ssafy.virtudy.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // mysql의 auto_inc 사용하기 위함
    private Long id; // PK

    @Column(nullable = false)
    @Builder.Default
    private String logId = UUID.randomUUID().toString(); // UUID

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
