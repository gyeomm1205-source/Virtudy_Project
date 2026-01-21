package com.ssafy.virtudy.common.init;

import com.ssafy.virtudy.group.domain.Group;
import com.ssafy.virtudy.group.domain.GroupMember;
import com.ssafy.virtudy.group.repository.GroupMemberRepository;
import com.ssafy.virtudy.group.repository.GroupRepository;
import com.ssafy.virtudy.member.domain.*;
import com.ssafy.virtudy.member.repository.MemberGameStatRepository;
import com.ssafy.virtudy.member.repository.MemberRepository;
import com.ssafy.virtudy.study.domain.RoomStatType;
import com.ssafy.virtudy.study.domain.RoomType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import com.ssafy.virtudy.study.repository.StudyRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local") // "local" 프로파일일 때만 실행(배포 서버 사고 방지)
public class DummyDataInit implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberGameStatRepository memberGameStatRepository;
    // Faker: 가짜 데이터 생성 라이브러리 (한국어 설정)
    private final Faker faker = new Faker(new Locale("ko"));
    private final Random random = new Random();
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        // 이미 데이터가 있으면 생성하지 않음 (선택 사항)
        if (memberRepository.count() > 0) {
            log.info("데이터가 이미 존재하여 더미 데이터 생성을 건너뜁니다.");
            return ;
        }

        log.info("더미 데이터 생성을 시작합니다...");

        // 1. 회원(Member) & 게임스탯(MemberGameStat) 생성 (100명)
        List<Member> members = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Member member = createMember(i);
            members.add(member);

            // 회원 저장
            memberRepository.save(member);

            // 게임 스탯 생성 및 저장
            createAndSaveGameStat(member);
        }

        // 2. 그룹(Group) 생성 (10개)
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // 랜덤한 리더 선정
            Member leader = members.get(random.nextInt(members.size()));
            Group group = createGroup(leader);
            groupRepository.save(group);
            groups.add(group);
        }

        // 3. 그룹 가입 (GroupMember) - 랜덤하게 가입시키기
        for (Group group : groups) {
            int joinCount = random.nextInt(10) + 1; // 그룹당 1~10명 가입
            for (int k = 0; k < joinCount; k++) {
                Member member = members.get(random.nextInt(members.size()));
                // 이미 가입했는지 체크 로직은 생략 (더미니까)
                GroupMember groupMember = GroupMember.builder()
                        .groupId(group)     // 연관관계 편의 메서드가 있다면 사용
                        .groupMemberId(UUID.randomUUID().toString())
                        .member(member)
                        .joinedAt(LocalDateTime.now().minusDays(random.nextInt(100)))
                        .build();
                groupMemberRepository.save(groupMember);
            }
        }

        // 4. 스터디룸(StudyRoom) 생성
        for (int i = 0; i < 20; i++) {
            Member owner = members.get(random.nextInt(members.size()));
            StudyRoom room = createStudyRoom(owner);
            studyRoomRepository.save(room);
        }

        log.info("✅ 더미 데이터 생성이 완료되었습니다. (Member: 100명, Group: 10개)");
    }

    private Member createMember(int index) {
        return Member.builder()
                .memberId("user" + index)
                .password("{noop}1234") // 테스트용 암호화 안 된 비번
                .email(faker.internet().emailAddress())
                .isVideoAgreed(true)
                .isServiceAgreed(true)
                .isPersonalAgreed(true)
                .nickName(faker.name().fullName()) // "김철수", "이영희" 등 생성
                .jobType(JobType.values()[random.nextInt(JobType.values().length)]) // Enum 랜덤
                .status(MemberStatType.ACTIVE)
                .avatarImageUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=" + index) // 랜덤 아바타 API
                .avatarGenCount(0)
                .build();
    }

    private void createAndSaveGameStat(Member member) {
        int score = random.nextInt(3000); // 0 ~ 3000 점
        int totalStudyTime = random.nextInt(10000);

        MemberGameStat stat = MemberGameStat.builder()
                .member(member)
                .point(score)
                .statId(UUID.randomUUID().toString())
                .totalStudyTime(totalStudyTime)
                .tierScore(score / 100) // 대충 계산
                .build();

        memberGameStatRepository.save(stat);

        // ⭐ [중요] Redis 랭킹에도 동기화 (바로 테스트 가능하도록)
        String rankingKey = "rank:season:1";
        redisTemplate.opsForZSet().add(rankingKey, String.valueOf(member.getId()), score);
    }

    private Group createGroup(Member leader) {
        return Group.builder()
                .groupId(UUID.randomUUID().toString())
                .leader(leader)
                .name(faker.team().name() + " 스터디")
                .description(faker.lorem().sentence())
                .region(faker.address().city())
                .groupTierScore(random.nextInt(500))
                .build();
    }

    private StudyRoom createStudyRoom(Member owner) {
        return StudyRoom.builder()
                .owner(owner)
                .roomId(UUID.randomUUID().toString())
                .title(faker.book().title() + " 같이 공부해요")
                .type(RoomType.values()[random.nextInt(RoomType.values().length)])
                .maxUser(random.nextInt(6) + 2) // 2~8명
                .status(RoomStatType.OPEN)
                .build();
    }
}
