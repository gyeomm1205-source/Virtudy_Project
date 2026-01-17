package com.ssafy.virtudy.member.domain;

import jakarta.persistence.*;

@Entity
public class MemberPreference {
    @Id
    @GeneratedValue
    private Long id; //PK

    @Column(nullable = false)
    private String prefId; // UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Member member; // FK

    @Column(nullable = false)
    private int averageHours = 0;

    @Column(nullable = false)
    private int targetHours = 0;

    @Column(nullable = false)
    private ActiveTimeType activeTime; // 새벽/오전/오후/저녁
}
