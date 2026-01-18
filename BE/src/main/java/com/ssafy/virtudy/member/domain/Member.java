package com.ssafy.virtudy.member.domain;

import com.ssafy.virtudy.common.BaseTimeEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.cglib.core.Local;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // mysql의 auto_inc 사용하기 위함
    private Long id; // PK

    @Column(nullable = false)
    private String memberId; // UUID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgreedType isAgreed;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'ACTIVE'")
    private MemberStatType status; // 가입한 상태, 탈퇴한 상태 vs 그냥 지울래 ??

    @Column(nullable = false)
    private String avatarImageUrl;
}
