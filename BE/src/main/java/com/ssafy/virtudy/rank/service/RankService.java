package com.ssafy.virtudy.rank.service;

import com.ssafy.virtudy.global.aop.LogExecutionTime;
import com.ssafy.virtudy.global.aop.Retry;
import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberGameStat;
import com.ssafy.virtudy.member.dto.AvatarResponse;
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
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankService {
    private final MemberRepository memberRepository;
    private final MemberGameStatRepository memberGameStatRepository;
    private final StudyRoomRepository studyRoomRepository;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RANK_PRIVATE_KEY = "rank:private:season:1";
    private static final String RANK_TEAM_KEY = "rank:team:season:1";
    private static final String ROOMTYPE_PRIVATE = "private";
    private static final int PAGE_SIZE = 10;

    @LogExecutionTime
    // 랭크 조회
    public List<RankDTO.Response> getRankRange(long start, long end, String type) {
        Set<ZSetOperations.TypedTuple<String>> tuples = null;

        // 6. 결과 조립
        List<RankDTO.Response> responseList = new ArrayList<>();

        if (type.equals(ROOMTYPE_PRIVATE)) {
            tuples = redisTemplate.opsForZSet().reverseRangeWithScores(RANK_PRIVATE_KEY, start, end);

            if (tuples == null || tuples.isEmpty()) return List.of();
// 3. 필요한 사용자 ID 목록 추출 (Redis 결과에서 ID만 뽑기)
            List<String> userIds = tuples.stream()
                    .map(ZSetOperations.TypedTuple::getValue)
                    .toList();

            // 4. DB에서 '필요한 회원 정보만' 조회 (성능 최적화 핵심 ✨)
            List<Member> members = memberRepository.findByMemberIdIn(userIds);

            // 5. 조회를 편하게 하기 위해 Map으로 변환 (Key: memberId, Value: Member)
            Map<String, Member> memberMap = members.stream()
                    .collect(Collectors.toMap(Member::getMemberId, Function.identity()));

            int currentRank = (int) start + 1;

            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                String userId = tuple.getValue();
                Double scoreVal = tuple.getScore();
                int score = (scoreVal != null) ? scoreVal.intValue() : 0;

                // DB에서 가져온 추가 정보 (없을 경우 대비해 null 체크 권장)
                Member member = memberMap.get(userId);
                if (member == null) continue; // 혹은 기본값 처리

                responseList.add(RankDTO.Response.builder()
                        .rank(currentRank++)
                        .nickName(member.getNickName()) // ✅ DB에서 가져온 닉네임 세팅
                        .email(member.getEmail())       // ✅ 필요한 경우 이메일도 세팅
                        .score(score)
                        .avatar(AvatarResponse.from(member.getAvatar()))
                        .tier(calculateTier(score))
                        .build());
            }
        } else {
            // 팀 랭킹
            tuples = redisTemplate.opsForZSet().reverseRangeWithScores(RANK_TEAM_KEY, start, end);

            if (tuples == null || tuples.isEmpty()) return List.of();

            List<String> teamIds = tuples.stream()
                    .map(ZSetOperations.TypedTuple::getValue)
                    .toList();


            // 4. DB에서 '필요한 회원 정보만' 조회 (성능 최적화 핵심 ✨)
            List<StudyRoom> studyRooms = studyRoomRepository.findStudyRoomsByRoomIdIn(teamIds);

            // 5. 조회를 편하게 하기 위해 Map으로 변환 (Key: memberId, Value: Member)
            Map<String, StudyRoom> studyRoomMap = studyRooms.stream()
                    .collect(Collectors.toMap(StudyRoom::getRoomId, Function.identity()));

            int currentRank = (int) start + 1;

            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                String teamId = tuple.getValue();
                Double scoreVal = tuple.getScore();

                // DB에서 가져온 추가 정보 (없을 경우 대비해 null 체크 권장)
                StudyRoom studyRoom = studyRoomMap.get(teamId);
                if (studyRoom == null) continue; // 혹은 기본값 처리

                int score = (scoreVal != null) ? scoreVal.intValue() : 0;

                responseList.add(RankDTO.Response.builder()
                        .rank(currentRank++)
                        .nickName(studyRoom.getTitle()) // ✅ DB에서 가져온 닉네임 세팅
                        .score(score)
                        .tier(calculateTier(score))
                        .build());
            }
        }

        return responseList;
    }
    @LogExecutionTime
    public RankDTO.Response getUserRankById(String userId, String type) {
        Member member = memberRepository.findByMemberId(userId)
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR));
        String nickName = null;
        Long rankIndex;
        AvatarResponse avatarDto = null;
        Double scoreVal;
        String email = null;
        if (type.equals(ROOMTYPE_PRIVATE)) {
            nickName = member.getNickName();
            rankIndex = redisTemplate.opsForZSet().reverseRank(RANK_PRIVATE_KEY, userId);
            scoreVal = redisTemplate.opsForZSet().score(RANK_PRIVATE_KEY, userId);
            avatarDto = AvatarResponse.from(member.getAvatar());
            email = member.getEmail();
        } else {
            // 최애 팀 깎고 와야됨.
            StudyRoom studyRoom = member.getFavoriteRoom();

            if (studyRoom == null) {
                return null;
            }

            Member owner = memberRepository.findByMemberId(studyRoom.getOwner().getMemberId())
                    .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR));
            nickName = studyRoom.getTitle();
            rankIndex = redisTemplate.opsForZSet().reverseRank(RANK_TEAM_KEY, studyRoom.getRoomId());
            scoreVal = redisTemplate.opsForZSet().score(RANK_TEAM_KEY, studyRoom.getRoomId());
            avatarDto = AvatarResponse.from(owner.getAvatar());
        }

        if (type.equals(ROOMTYPE_PRIVATE) && rankIndex == null) {
            return null;
        }
        return RankDTO.Response.builder()
                .nickName(nickName)
                .email(email)
                .rank(rankIndex.intValue() + 1)
                .score(scoreVal.intValue())
                .avatar(avatarDto)
                .tier(calculateTier(scoreVal))
                .build();
    }

    /**
     * nickname, title로 값을 받아와서 거기에 맞는 사람들 List형태로 리턴 시켜준다.
     * @param name
     * @param type
     * @return
     */
    @LogExecutionTime
    public List<RankDTO.Response> getUserRankByNickName(String name, String type) {
        Long rankIndex = null;
        Double scoreVal = 0.0;
        String nickName = null;
        String userId = null;
        AvatarResponse avatarDto = null;
        List<RankDTO.Response> responseList = new ArrayList<>();
        if (type.equals(ROOMTYPE_PRIVATE)) {
            List<Member> memberList = memberRepository.findByNickName(name);

            for (Member tempMember : memberList) {
                userId = tempMember.getMemberId();
                // 1. 내 등수 조회 (주의: 0등부터 시작함)
                // "거꾸로(reverse) 세어봐. 점수 높은 게 1등이니까"
                rankIndex = redisTemplate.opsForZSet().reverseRank(RANK_PRIVATE_KEY, userId);

                if (rankIndex == null) {
                    continue;
                }
                // 2. 내 점수 조회
                scoreVal = redisTemplate.opsForZSet().score(RANK_PRIVATE_KEY, userId);

                nickName = name;
                avatarDto = AvatarResponse.from(tempMember.getAvatar());
                responseList.add(
                        RankDTO.Response.builder()
                                .nickName(nickName)
                                .email(tempMember.getEmail())
                                .rank(rankIndex.intValue() + 1)
                                .score(scoreVal.intValue())
                                .avatar(avatarDto)
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
                avatarDto = AvatarResponse.from(tempStudyRoom.getOwner().getAvatar());
                responseList.add(
                        RankDTO.Response.builder()
                                .nickName(nickName)
                                .email(tempStudyRoom.getOwner().getEmail())
                                .avatar(avatarDto)
                                .rank(rankIndex.intValue() + 1)
                                .score(scoreVal.intValue())
                                .tier(calculateTier(scoreVal))
                                .build()
                );
            }
        }
        if (responseList.isEmpty()) {
            throw new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR);
        }
        // 4. 결과 리턴
        return responseList;
    }
    @LogExecutionTime
    public List<RankDTO.Response> getRankByPage(int page, String type) {
        long start = (long) page * PAGE_SIZE;
        long end = start + PAGE_SIZE - 1;
        return getRankRange(start, end, type);
    }
    @LogExecutionTime
    public List<RankDTO.Response> getTop5Rank(String type) {
        return getRankRange(0, 4, type);
    }

    /**
     * 티어 계산 함수 추후 변경
     * @param score
     * @return
     */
    public RankDTO.Tier calculateTier(double score) {
        if (score >= 9000) return RankDTO.Tier.DIAMOND;
        if (score >= 5000) return RankDTO.Tier.PLATINUM; // Platinum 추가 (RankDTO.Tier에 있다면)
        if (score >= 3000) return RankDTO.Tier.GOLD;
        if (score >= 1000) return RankDTO.Tier.SILVER;
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

        // 1. 임시 키에 데이터 저장 ( 기존 찌꺼기 방지 위해 임시 키는 먼저 삭제)
        redisTemplate.delete(tempPrivateKey);
        redisTemplate.delete(tempTeamKey);

        List<MemberGameStat> stats = memberGameStatRepository.findAll();
        for (MemberGameStat stat : stats) {
            redisTemplate.opsForZSet().add(tempPrivateKey, stat.getMember().getMemberId(), stat.getTierScore());
        }

        List<StudyRoom> studyRoomList = studyRoomRepository.findAll();
        for (StudyRoom studyRoom : studyRoomList) {
            redisTemplate.opsForZSet().add(tempTeamKey, studyRoom.getRoomId(), studyRoom.getRoomTierScore());
        }

        // 임시 키를 진짜 키로 이름 변경 (덮어씌우기)
        redisTemplate.rename(tempPrivateKey, RANK_PRIVATE_KEY);
        redisTemplate.rename(tempTeamKey, RANK_TEAM_KEY);

        log.info("랭킹 업데이트 완료(깜빡임 없음)");
    }

    // 기본값 (3회) 재시도
    @Retry(value = 10)
    public void refreshUserScore(String memberId, double newTotalScore) {
        redisTemplate.opsForZSet().add(RANK_PRIVATE_KEY, memberId, newTotalScore);
        log.info("User Rank Updated: id={}, score={}", memberId, newTotalScore);
    }

    // 기본값 (3회) 재시도
    @Retry(value = 10)
    public void refreshTeamScore(String teamId, double newTotalScore) {
        redisTemplate.opsForZSet().add(RANK_TEAM_KEY, teamId, newTotalScore);
        log.info("Team Rank Updated: id={}, score={}", teamId, newTotalScore);
    }
}
