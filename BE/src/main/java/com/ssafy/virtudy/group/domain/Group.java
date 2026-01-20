package com.ssafy.virtudy.group.domain;

import com.ssafy.virtudy.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
/**
 *  MySql 예약어 중에 Group 이 있어서 충돌 남.
 *  Group => studyGroup 변경
 */
@Table(name = "studyGroup")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // mysql의 auto_inc 사용하기 위함
    private Long id; // PK

    @Column(nullable = false)
    private String groupId; // UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // optional=false가 not null 역할을 함
    @JoinColumn(name = "MEMBER_ID")
    private Member leader; // FK

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private int groupTierScore = 0;

}
