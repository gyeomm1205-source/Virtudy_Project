package com.ssafy.virtudy.study.domain;

import com.ssafy.virtudy.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_room_id")
    private StudyRoom studyRoom;

    private StudyMember(Member member, StudyRoom studyRoom) {
        this.member = member;
        this.studyRoom = studyRoom;
    }

    public static StudyMember of(Member member, StudyRoom studyRoom) {
        return new StudyMember(member, studyRoom);
    }
}
