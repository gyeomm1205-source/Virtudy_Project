package com.ssafy.virtudy.member.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // mysql의 auto_inc 사용하기 위함
    private Long id; //PK

    // TODO: prefId를 AuthService에서 random생성하고 있음. unique=true를 줘서 중복은 막았는데 ㄱㅊ?
    @Column(nullable = false, unique = true)
    private String prefId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Member member; // FK

    /**
     * 학습 성향
    */
    @Column(nullable = false)
    private int targetHours = 0; // 1일 목표 공부시간 (분 단위)

    @Column(nullable = false)
    private int averageHours = 0; // 1일 평균 공부시간

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActiveTimeType activeTime; // 새벽/오전/오후/저녁

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StudyType studyType; // MARATHON, SPRINTER

}
