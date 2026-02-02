package com.ssafy.virtudy.member.domain;

import com.ssafy.virtudy.global.BaseTimeEntity;
import com.ssafy.virtudy.study.domain.StudyRoom;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
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

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Avatar avatar;

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
    @BatchSize(size = 100) // N+1 문제 완화를 위한 Batch Fetching
    private List<MemberPreference> preferences = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    @BatchSize(size = 100) // N+1 문제 완화를 위한 Batch Fetching
    private List<MemberGameStat> gameStats = new ArrayList<>();

    public void setFavoriteRoom(StudyRoom studyRoom) {
        this.favoriteRoom = studyRoom;
    }

    public void updateProfile(String nickName, JobType jobType) {
        if (nickName != null) this.nickName = nickName;
        if (jobType != null) this.jobType = jobType;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    // [추가] 회원 상태 변경 편의 메서드 (탈퇴/복구 시 사용)
    public void updateStatus(MemberStatType status) {
        this.status = status;
    }

    // 재가입 편의 메서드: setter 굳이 ? JPA dirty checking 으로도 충분히 가능할 듯 ㅇㅇ
    public void rejoin(String nickName,
                       JobType jobType,
                       boolean isServiceAgreed,
                       boolean isVideoAgreed,
                       boolean isPersonalAgreed) {
        this.nickName = nickName;
        this.jobType = jobType;
        this.isServiceAgreed = isServiceAgreed;
        this.isVideoAgreed = isVideoAgreed;
        this.isPersonalAgreed = isPersonalAgreed;
        // 강제 초기화 부분
        this.status = MemberStatType.ACTIVE; // 상태 부활!
        this.avatarGenCount = 0; // 초기화 필요한 필드 리셋
        this.avatar = null;
        this.favoriteRoom = null;
    }


}
