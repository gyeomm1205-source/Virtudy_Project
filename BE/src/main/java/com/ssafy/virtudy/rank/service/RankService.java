package com.ssafy.virtudy.rank.service;

<<<<<<< HEAD
import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.Avatar;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberGameStat;
import com.ssafy.virtudy.member.dto.AvatarResponse;
import com.ssafy.virtudy.member.dto.MemberDto;
=======
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberGameStat;
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
import com.ssafy.virtudy.member.repository.MemberGameStatRepository;
import com.ssafy.virtudy.member.repository.MemberRepository;
import com.ssafy.virtudy.rank.dto.RankDTO;
import com.ssafy.virtudy.study.domain.StudyRoom;
import com.ssafy.virtudy.study.repository.StudyRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankService {
    private final MemberRepository memberRepository;
    private final MemberGameStatRepository memberGameStatRepository;
    private final StudyRoomRepository studyRoomRepository;

    private final MemberRepository memberRepository;
    private final MemberGameStatRepository memberGameStatRepository;
    private final StudyRoomRepository studyRoomRepository;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RANK_PRIATE_KEY = "rank:private:season:1";
    private static final String RANK_TEAM_KEY = "rank:team:season:1";
    private static final String ROOMTYPE_PRIVATE = "private";
    private static final int PAGE_SIZE = 10;

    // 랭크 조회
    public List<RankDTO.Response> getRankRange(long start, long end, String type) {
        Set<ZSetOperations.TypedTuple<String>> tuples = null;

<<<<<<< HEAD
        if (type.equals(ROOMTYPE_PRIVATE)) {
            tuples = redisTemplate.opsForZSet().reverseRangeWithScores(RANK_PRIATE_KEY, start, end);
        } else {
            // 팀 랭킹
            tuples = redisTemplate.opsForZSet().reverseRangeWithScores(RANK_TEAM_KEY, start, end);
        }

        if (tuples == null || tuples.isEmpty()) return List.of();
// 3. 필요한 사용자 ID 목록 추출 (Redis 결과에서 ID만 뽑기)
        List<String> userIds = tuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .toList();
=======

        if (type.equals(ROOMTYPE_PRIVATE)) {
            tuples = redisTemplate.opsForZSet().reverseRangeWithScores(RANK_PRIATE_KEY, start, end);
        } else {
            // 팀 랭킹
            tuples = redisTemplate.opsForZSet().reverseRangeWithScores(RANK_TEAM_KEY, start, end);
        }

        System.out.println(tuples);

        if (tuples == null || tuples.isEmpty()) return List.of();
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)

        // 4. DB에서 '필요한 회원 정보만' 조회 (성능 최적화 핵심 ✨)
        List<Member> members = memberRepository.findByMemberIdIn(userIds);

        // 5. 조회를 편하게 하기 위해 Map으로 변환 (Key: memberId, Value: Member)
        Map<String, Member> memberMap = members.stream()
                .collect(Collectors.toMap(Member::getMemberId, Function.identity()));

        // 6. 결과 조립
        List<RankDTO.Response> responseList = new ArrayList<>();
        int currentRank = (int) start + 1;

<<<<<<< HEAD
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
=======
        int currentRank = (int) start + 1;

        // 2. 반복문 돌면서 DTO로 변환
        for (ZSetOperations.TypedTuple<String> tuple: tuples) {
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
            String userId = tuple.getValue();
            Double scoreVal = tuple.getScore();
            int score = (scoreVal != null) ? scoreVal.intValue() : 0;

            // DB에서 가져온 추가 정보 (없을 경우 대비해 null 체크 권장)
            Member member = memberMap.get(userId);
            if (member == null) continue; // 혹은 기본값 처리

            responseList.add(RankDTO.Response.builder()
                    .id(userId)
                    .rank(currentRank++)
                    .nickName(member.getNickName()) // ✅ DB에서 가져온 닉네임 세팅
                    .email(member.getEmail())       // ✅ 필요한 경우 이메일도 세팅
                    .score(score)
                    .avatar(AvatarResponse.from(member.getAvatar()))
                    .tier(calculateTier(score))
                    .build());
        }
        return responseList;
    }

    public RankDTO.Response getUserRankById(String userId, String type) {
        Member member = memberRepository.findByMemberId(userId)
<<<<<<< HEAD
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR));
        String nickName = null;
        Long rankIndex;
        AvatarResponse avatarDto = null;
        Double scoreVal;
        if (type.equals(ROOMTYPE_PRIVATE)) {
            nickName = member.getNickName();
            rankIndex = redisTemplate.opsForZSet().reverseRank(RANK_PRIATE_KEY, userId);
            scoreVal = redisTemplate.opsForZSet().score(RANK_PRIATE_KEY, userId);
            avatarDto = AvatarResponse.from(member.getAvatar());
        } else {
            // 최애 팀 깎고 와야됨.
            StudyRoom studyRoom = member.getFavoriteRoom();
            Member owner = memberRepository.findByMemberId(studyRoom.getOwner().getMemberId())
                    .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR));

            rankIndex = redisTemplate.opsForZSet().reverseRank(RANK_TEAM_KEY, studyRoom.getRoomId());
            scoreVal = redisTemplate.opsForZSet().score(RANK_TEAM_KEY, studyRoom.getRoomId());
            avatarDto = AvatarResponse.from(owner.getAvatar());
        }

        if (rankIndex == null) {
            return null;
        }
=======
                .orElseThrow(() -> new IllegalArgumentException("찾는 아이디가 없습니다."));
        String nickName = null;
        Long rankIndex;
        Double scoreVal ;

        if (type.equals(ROOMTYPE_PRIVATE)) {
            nickName = member.getNickName();
            rankIndex = redisTemplate.opsForZSet().reverseRank(RANK_PRIATE_KEY, userId);
            scoreVal = redisTemplate.opsForZSet().score(RANK_PRIATE_KEY, userId);
        } else {
            // 최애 팀 깎고 와야됨.
            StudyRoom studyRoom = member.getFavoriteRoom();

            nickName = studyRoom.getRoomId();
            rankIndex = redisTemplate.opsForZSet().reverseRank(RANK_PRIATE_KEY, userId);
            scoreVal = redisTemplate.opsForZSet().score(RANK_TEAM_KEY, userId);
        }
        if (rankIndex == null) {
            return null;
        }

>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
        return RankDTO.Response.builder()
                .id(userId)
                .nickName(nickName)
                .rank(rankIndex.intValue() + 1)
                .score(scoreVal.intValue())
                .avatar(avatarDto)
                .tier(calculateTier(scoreVal))
                .build();
    }

<<<<<<< HEAD
=======

>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
    /**
     * nickname, title로 값을 받아와서 거기에 맞는 사람들 List형태로 리턴 시켜준다.
     * @param name
     * @param type
     * @return
     */
    public List<RankDTO.Response> getUserRankByNickName(String name, String type) {
<<<<<<< HEAD
=======

>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
        Long rankIndex = null;
        Double scoreVal = 0.0;
        String nickName = null;
        String userId = null;
<<<<<<< HEAD
        AvatarResponse avatarDto = null;
=======
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
        List<RankDTO.Response> responseList = new ArrayList<>();
        if (type.equals(ROOMTYPE_PRIVATE)) {
            List<Member> memberList = memberRepository.findByNickName(name);

<<<<<<< HEAD
=======

>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
            for (Member tempMember : memberList) {
                userId = tempMember.getMemberId();
                // 1. 내 등수 조회 (주의: 0등부터 시작함)
                // "거꾸로(reverse) 세어봐. 점수 높은 게 1등이니까"
                rankIndex = redisTemplate.opsForZSet().reverseRank(RANK_PRIATE_KEY, userId);

                if (rankIndex == null) {
                    continue;
                }
                // 2. 내 점수 조회
                scoreVal = redisTemplate.opsForZSet().score(RANK_PRIATE_KEY, userId);

                nickName = name;
<<<<<<< HEAD
                avatarDto = AvatarResponse.from(tempMember.getAvatar());
=======

>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
                responseList.add(
                        RankDTO.Response.builder()
                                .id(userId)
                                .nickName(nickName)
<<<<<<< HEAD
                                .email(tempMember.getEmail())
                                .rank(rankIndex.intValue() + 1)
                                .score(scoreVal.intValue())
                                .avatar(avatarDto)
=======
                                .rank(rankIndex.intValue() + 1)
                                .score(scoreVal.intValue())
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
                                .tier(calculateTier(scoreVal))
                                .build()
                );
            }
        } else {
            // 검색
            List<StudyRoom> optionalList = studyRoomRepository.findByTitle(name);

            // title이 하나일 경우.
            for (StudyRoom tempStudyRoom : optionalList) {

                userId = tempStudyRoom.getRoomId();
                nickName = tempStudyRoom.getTitle();
                // 최애 팀 갖고오기
                // 1. 내 팀 등수 조회 => 최애팀 기준으로 랭킹을 줘야함.
                rankIndex = redisTemplate.opsForZSet().reverseRank(RANK_TEAM_KEY, userId);
                // 2. 내 팀 점수 조회
                if (rankIndex == null) {
                    continue;
                }
                scoreVal = redisTemplate.opsForZSet().score(RANK_TEAM_KEY, userId);
<<<<<<< HEAD
                avatarDto = AvatarResponse.from(tempStudyRoom.getOwner().getAvatar());
=======
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
                responseList.add(
                        RankDTO.Response.builder()
                                .id(userId)
                                .nickName(nickName)
<<<<<<< HEAD
                                .email(tempStudyRoom.getOwner().getEmail())
                                .avatar(avatarDto)
=======
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
                                .rank(rankIndex.intValue() + 1)
                                .score(scoreVal.intValue())
                                .tier(calculateTier(scoreVal))
                                .build()
                );
            }
        }
<<<<<<< HEAD
        if (responseList.isEmpty()) {
            throw new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR);
=======

        if (responseList.isEmpty()) {
            throw new IllegalArgumentException("검색한 아이디가 없습니다.");
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
        }
        // 4. 결과 리턴
        return responseList;
    }

<<<<<<< HEAD
    public List<RankDTO.Response> getRankByPage(int page, String type) {
        long start = (long) page * PAGE_SIZE;
        long end = start + PAGE_SIZE - 1;
        return getRankRange(start, end, type);
    }

    public List<RankDTO.Response> getTop5Rank(String type) {
        return getRankRange(0, 4, type);
    }

=======
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
    /**
     * 티어 계산 함수 추후 변경
     * @param score
     * @return
     */
<<<<<<< HEAD
    public RankDTO.Tier calculateTier(double score) {
        if (score >= 9000) return RankDTO.Tier.DIAMOND;
        if (score >= 5000) return RankDTO.Tier.PLATINUM; // Platinum 추가 (RankDTO.Tier에 있다면)
        if (score >= 3000) return RankDTO.Tier.GOLD;
        if (score >= 1000) return RankDTO.Tier.SILVER;
=======
    private RankDTO.Tier calculateTier(double score) {
        if (score >= 1000) return RankDTO.Tier.DIAMOND;
        if (score >= 500) return RankDTO.Tier.GOLD;
        if (score >= 100) return RankDTO.Tier.SILVER;
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
        return RankDTO.Tier.BRONZE;
    }

    /**
     * 랭킹 업데이트 하는 스케줄러 함수 본체
     */
    @Transactional
    public void updateRankingBatch() {
        // 랭킹 업데이트하는 함수.
        // 싹 다 밀고 가 ?
        String tempTeamKey = "rank:team:season:1:temp";
        String tempPrivateKey = "rank:private:season:1:temp";

<<<<<<< HEAD
=======
    public List<RankDTO.Response> getRankByPage(int page, String type) {
        long start = (long) page * PAGE_SIZE;
        long end = start + PAGE_SIZE - 1;
        return getRankRange(start, end, type);
    }

    public List<RankDTO.Response> getTop5Rank(String type) {
        return getRankRange(0, 4, type) ;
    }

    /**
     * 랭킹 업데이트 하는 스케줄러 함수 본체
     */
    @Transactional
    public void updateRankingBatch() {
        // 랭킹 업데이트하는 함수.
        // 싹 다 밀고 가 ?
        String tempTeamKey = "rank:team:season:1:temp";
        String tempPrivateKey = "rank:private:season:1:temp";

>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
        // 1. 임시 키에 데이터 저장 ( 기존 찌꺼기 방지 위해 임시 키는 먼저 삭제)
        redisTemplate.delete(tempPrivateKey);
        redisTemplate.delete(tempTeamKey);

        List<MemberGameStat> stats = memberGameStatRepository.findAll();
        for (MemberGameStat stat : stats) {
            redisTemplate.opsForZSet().add(tempPrivateKey, stat.getMember().getNickName(), stat.getTierScore());
        }

        List<StudyRoom> studyRoomList = studyRoomRepository.findAll();
<<<<<<< HEAD
        for (StudyRoom studyRoom : studyRoomList) {
=======
        for(StudyRoom studyRoom : studyRoomList)  {
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
            redisTemplate.opsForZSet().add(tempTeamKey, studyRoom.getTitle(), studyRoom.getRoomTierScore());
        }

        // 임시 키를 진짜 키로 이름 변경 (덮어씌우기)
        redisTemplate.rename(tempPrivateKey, RANK_PRIATE_KEY);
        redisTemplate.rename(tempTeamKey, RANK_TEAM_KEY);

        log.info("랭킹 업데이트 완료(깜빡임 없음)");
<<<<<<< HEAD
=======

>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
    }
}
