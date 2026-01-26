package com.ssafy.virtudy.study.domain;

import com.ssafy.virtudy.common.BaseTimeEntity;
import com.ssafy.virtudy.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudySession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // mysql의 auto_inc 사용하기 위함
    private Long id; // PK

    @Column(nullable = false, unique = true)
    private String sessionId; // UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Member member; // FK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ROOM_ID")
    private StudyRoom room; // FK

    @Column(nullable = false, updatable = false)
    private LocalDateTime startTime; // 세션 시작시각
<<<<<<< HEAD

    @Column(nullable = false)
    private int sessionRealStudyTime = 0;
=======
>>>>>>> dad092f (add: 티어 및 리포트 관련 API)

    @Column(nullable = false)
    private int sessionRealStudyTime = 0;

    @Column
    private LocalDateTime endTime; // 세션 종료시각

    @Builder
    private StudySession(Member member, StudyRoom room) {
        this.sessionId = UUID.randomUUID().toString();
        this.member = member;
        this.room = room;
        this.startTime = LocalDateTime.now();
    }

    public void close() {
        this.endTime = LocalDateTime.now();
    }
}
