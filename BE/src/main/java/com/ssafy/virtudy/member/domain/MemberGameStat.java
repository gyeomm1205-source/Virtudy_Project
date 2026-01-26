package com.ssafy.virtudy.member.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberGameStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // mysql의 auto_inc 사용하기 위함
    private Long id;// PK

    @Column(nullable = false)
    private String statId; // UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Member member; // FK

    @Column(nullable = false)
    @Builder.Default
    private int point = 0;

    @Column(nullable = false)
    @Builder.Default
    private int totalStudyTime = 0;

    @Column(nullable = false)
    @Builder.Default
    private int tierScore = 0;

    public void updateTierScore(int newScore) {
        this.tierScore = newScore;
    }
}
