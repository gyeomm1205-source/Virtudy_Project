package com.ssafy.virtudy.study.domain;

import com.ssafy.virtudy.common.BaseTimeEntity;
import com.ssafy.virtudy.member.domain.Member;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
public class StudyRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue
    private Long id; // PK

    @Column(nullable = false)
    private String roomId; // UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Member owner; // FK

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type; // PUBLIC, PRIVATE

    @Column(nullable = false)
    private int maxUser = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'OPEN'")
    private RoomStatType status; // OPEN/ CLOSED
}
