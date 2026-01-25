package com.ssafy.virtudy.member.domain;

import com.ssafy.virtudy.common.BaseTimeEntity;
import com.ssafy.virtudy.study.domain.StudyRoom;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // mysql의 auto_inc 사용하기 위함
    private Long id; // PK

    @Column(nullable = false, unique = true)
    private String memberId; // UUID

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private AgreedType isAgreed;

    @Column(nullable = false)
    private boolean isServiceAgreed;

    @Column(nullable = false)
    private boolean isPersonalAgreed;

    @Column(nullable = false)
    private boolean isVideoAgreed;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false )
    private String nickName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType; // STUDENT, JOB_SEEKER, WORKER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'ACTIVE'")
    private MemberStatType status; // 가입한 상태, 탈퇴한 상태 vs 그냥 지울래 ??

    @Column(nullable = false)
    private String avatarImageUrl;

    // Member.java 내부에 필드 추가
    @Column(nullable = false)
    @Builder.Default
    private Integer avatarGenCount = 0; // 아바타 생성 횟수 (기본값 0)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "favorite_room_id")
    private StudyRoom favoriteRoom;

    /**
     * [CASCADE 설정 추가]
     * mappedBy = "member": 자식 엔티티(MemberPreference)에 있는 'member' 필드가 주인임을 명시
     * cascade = CascadeType.ALL: 부모(Member)가 저장/삭제/수정될 때 자식도 같이 처리
     * orphanRemoval = false
     */
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default // 빌더 패턴 사용 시 리스트 초기화 방지
    private List<MemberPreference> preferences = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MemberGameStat> gameStats = new ArrayList<>();

    public void setFavoriteRoom(StudyRoom studyRoom) {
        this.favoriteRoom = studyRoom;
    }

    public void updateProfile(String nickName, JobType jobType) {
        this.nickName = nickName;
        this.jobType = jobType;
    }
}
