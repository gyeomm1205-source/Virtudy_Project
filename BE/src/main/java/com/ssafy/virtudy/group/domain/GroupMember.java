package com.ssafy.virtudy.group.domain;

import com.ssafy.virtudy.member.domain.Member;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class GroupMember {

    @Id
    @GeneratedValue
    private Long id; // PK

    @Column(nullable = false)
    private String groupMemberId; // UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "GROUP_ID")
    private Group groupId; // FK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID")
    private Member member; // FK

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}
