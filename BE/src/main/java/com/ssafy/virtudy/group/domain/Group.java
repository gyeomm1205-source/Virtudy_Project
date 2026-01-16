package com.ssafy.virtudy.group.domain;

import jakarta.persistence.*;

@Entity
public class Group {

    @Id
    @GeneratedValue
    private Long id; // PK

    @Column(nullable = false)
    private String groupId; // UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // optional=false가 not null 역할을 함
    @JoinColumn(name = "MEMBER_ID")
    private Long leaderId; // FK

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private int groupTierScore = 0;

}
