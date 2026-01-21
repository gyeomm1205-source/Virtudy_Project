package com.ssafy.virtudy.common.init;

import com.ssafy.virtudy.group.repository.RoomMemberRepository;
import com.ssafy.virtudy.member.domain.*;
import com.ssafy.virtudy.member.repository.AvatarRepository;
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
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 추가 권장

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
@Order(1)
public class DummyDataInit implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final MemberGameStatRepository memberGameStatRepository;
    private final AvatarRepository avatarRepository;

    private final Faker faker = new Faker(new Locale("en"));
    private final Random random = new Random();
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RANK_PRIVATE_KEY = "rank:private:season:1";
    private static final String RANK_TEAM_KEY = "rank:team:season:1";

    @Override
    // @Transactional // 필요시 주석 해제 (Lazy Loading 문제 발생 시)
    public void run(String... args) throws Exception {

        // 1. ddl-auto: create 라면 DB는 비어있지만, Redis는 그대로일 수 있음.
        //    확실한 초기화를 위해 Redis 키도 삭제해줍니다.
        redisTemplate.delete(RANK_TEAM_KEY);
        redisTemplate.delete(RANK_PRIVATE_KEY);
        log.info("🧹 Redis 랭킹 데이터({}) 초기화 완료", RANK_PRIVATE_KEY);

        // 2. 중복 방지 (ddl-auto: create면 항상 0이라 통과함)
        if (memberRepository.count() > 0) {
            log.info("ℹ️ DB에 데이터가 존재하여 생성을 건너뜁니다.");
            return;
        }
        log.info("🚀 더미 데이터 생성을 시작합니다...");
        List<Member> savedMembers = new ArrayList<>(); // 저장된 멤버 리스트

        // 3. 회원 생성 루프
        for (int i = 0; i < 100; i++) {
            Member member = createMember(i);
            // 여기서 아바타 생성할거임.
            Avatar avatar = createAvater(i, member);
            member.setAvatar(avatar);

            // ⭐ [핵심 수정] save()가 반환한 객체(savedMember)를 받아야 ID가 들어있습니다!
            Member savedMember = memberRepository.save(member);
            savedMembers.add(savedMember);

            // ⭐ [핵심 수정] savedMember를 넘겨야 ID가 null이 아닙니다.
            createAndSaveGameStat(savedMember, RANK_PRIVATE_KEY);
        }
        // 4. 스터디룸 생성
        for (int i = 0; i < 20; i++) {
            // 저장된 멤버 목록에서 랜덤으로 주인을 뽑음
            Member owner = savedMembers.get(random.nextInt(savedMembers.size()));
            StudyRoom room = createStudyRoom(owner);
            studyRoomRepository.save(room);
        }
        List<Member> members = memberRepository.findAll();
        int index = 0;
        for (Member member: members) {
            Long studyIndex = (long) ((index++ % 20) + 1);

            StudyRoom room = studyRoomRepository.getReferenceById(studyIndex);
            member.setFavoriteRoom(room);
            memberRepository.save(member);
        }
        log.info("✅ 더미 데이터 생성 완료! (Member: 100, Room: 20)");
    }

    private Avatar createAvater(int i, Member member) {
        // 1. 더미 데이터 재료 준비 (배열)
        String[] hairFronts = {
                "bang",
                "center_part",
                "hair_front_none",
                "hair_front_short",
                "hair_front_side_part"
        };
        String[] hairBacks = {
                "hair_back_bob",
                "hair_back_long_curly",
                "hair_back_long_straight",
                "hair_back_long_lowtail",
                "hair_back_short"
        };
        String[] eyesList = {
                "eyes_cat",
                "eyes_droopy",
                "eyes_round"
        };
        // 안경은 있을 수도 있고 없을 수도 있음 (확률적으로 처리하거나 번갈아가며)
        String[] glassesList = {"accessory_glasses", "none", "none", "none"};
        String[] outfits = {
                "outfit_knit",
                "outfit_round_neck",
                "outfit_shirt"
        };
        // 색상 팔레트
        String[] hairColors = {"#111111", "#3B3024", "#825336", "#9A7248", "#8C8C8C"};
        String[] clothesColors = {"#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#000000"};

        // 2. 인덱스(i)를 이용해 순서대로 선택
        String hairFront = hairFronts[i % hairFronts.length];
        String hairBack = hairBacks[i % hairBacks.length];
        String eyes = eyesList[i % eyesList.length];
        String glasses = glassesList[i % glassesList.length];
        String outfit = outfits[i % outfits.length];

        String hairColor = hairColors[i % hairColors.length];
        String clothesColor = clothesColors[i % clothesColors.length];

        // 3. 빌더 반환
        return Avatar.builder()
                .member(member)
                .hairFront(hairFront)       // 예: "bang"
                .hairBack(hairBack)         // 예: "hair_back_bob"
                .hairColor(hairColor)       // 예: "#3B3024"
                .eyes(eyes)                 // 예: "eyes_cat"
                .glasses(glasses)           // 예: "accessory_glasses" 또는 "none"
                .clothes(outfit)            // 예: "outfit_knit"
                .clothesColor(clothesColor) // 예: "#FF0000"
                .build();
    }

    private Member createMember(int index) {
        String uniqueEmail = index + "_" + faker.internet().emailAddress();
        String uuid = UUID.randomUUID().toString();
        return Member.builder()
<<<<<<< HEAD
                .memberId(uuid)
                .password("{noop}1234")
                .email(uniqueEmail)
                .isVideoAgreed(true)
                .isServiceAgreed(true)
                .isPersonalAgreed(true)
                .nickName(faker.name().name())
                .jobType(JobType.values()[random.nextInt(JobType.values().length)])
=======
                .memberId("user" + index)
                .password("{noop}1234") // 테스트용 암호화 안 된 비번
                .email(faker.internet().emailAddress())
                .isVideoAgreed(true)
                .isServiceAgreed(true)
                .isPersonalAgreed(true)
                .nickName(faker.name().fullName()) // "김철수", "이영희" 등 생성
                .jobType(JobType.values()[random.nextInt(JobType.values().length)]) // Enum 랜덤
>>>>>>> eb6376f (fix: 약관 동의 필드 변경; 3개로 나눔)
                .status(MemberStatType.ACTIVE)
                // avatarImage fk로 생성.
                .avatarGenCount(0)
                .build();
    }

    private void createAndSaveGameStat(Member member, String rankingKey) {
        int score = random.nextInt(3000);
        int totalStudyTime = random.nextInt(10000);

        MemberGameStat stat = MemberGameStat.builder()
                .member(member)
                .point(score)
                .statId(UUID.randomUUID().toString())
                .totalStudyTime(totalStudyTime)
                .tierScore(score / 100)
                .build();

        memberGameStatRepository.save(stat);
        // Redis에 저장
        // member.getMemberId() (예: "user0") 또는 member.getId() (PK) 사용
        // 여기서는 memberId("user0")를 사용한다고 가정
        if (member.getMemberId() != null) {
            redisTemplate.opsForZSet().add(rankingKey, member.getMemberId(), score);
        } else {
            log.warn("⚠️ Member ID is null for member: {}", member);
        }
    }

    private StudyRoom createStudyRoom(Member owner) {
        return StudyRoom.builder()
                .owner(owner)
                .roomId(UUID.randomUUID().toString())
                .title(faker.book().title() + " 같이 공부해요")
                .type(RoomType.values()[random.nextInt(RoomType.values().length)])
                .description(faker.lorem().sentence())
                .roomTierScore(random.nextInt(500))
                .status(RoomStatType.OPEN)
                .build();
    }
}