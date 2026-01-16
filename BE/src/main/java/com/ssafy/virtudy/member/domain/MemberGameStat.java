package com.ssafy.virtudy.member.domain;

import jakarta.persistence.*;

@Entity
public class MemberGameStat {

    @Id
    @GeneratedValue
    private Long id;// PK

    @Column(nullable = false)
    private String statId; // UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Long memberId; // FK

    @Column(nullable = false)
    private int point = 0;

    @Column(nullable = false)
    private int totalStudyTime = 0;

    @Column(nullable = false)
    private int tierScore = 0;
}
