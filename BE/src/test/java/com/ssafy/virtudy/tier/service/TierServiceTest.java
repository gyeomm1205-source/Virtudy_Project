package com.ssafy.virtudy.tier.service;

import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberGameStat;
import com.ssafy.virtudy.member.repository.MemberGameStatRepository;
import com.ssafy.virtudy.member.repository.MemberRepository;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.dto.StudyAnalysisResult;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import com.ssafy.virtudy.study.service.StudyAnalysisService;
import com.ssafy.virtudy.tier.dto.TierResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TierServiceTest {

    @InjectMocks
    private TierService tierService;

    @Mock
    private StudySessionRepository studySessionRepository;
    @Mock
    private MemberGameStatRepository memberGameStatRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private StudyAnalysisService studyAnalysisService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ZSetOperations<String, String> zSetOperations;


    /**
     * (1) 내 티어 조회에 성공한 경우
     *  * 1. given: 목업 데이터에 memberId와 그 회원에 gamestat를 둔다.
     *  * 2. when: getMyTier를 실행했을 때
     *  * 3. then: 반환된 TierResponse와 객체의 내용이 일치해야 한다.
     */
    @Test
    @DisplayName("내 티어 조회 성공")
    void getMyTier_Success() {
        // given
        String memberId = "test-uuid";
        Member member = createMember(memberId);
        MemberGameStat stat = createMemberGameStat(member, 45000, 100);

        given(memberRepository.findByMemberId(memberId)).willReturn(Optional.of(member));
        given(memberGameStatRepository.findByMember(member)).willReturn(Optional.of(stat));

        // when
        TierResponse response = tierService.getMyTier(memberId);

        // then
        assertThat(response)
                .extracting("nickname", "tierScore", "tierRank", "totalStudyTime")
                .containsExactly("TestUser", 45000, "GOLD", 100);
    }

    /**
     *  * (2) 회원은 존재하지만 게임에 스탯 데이터가 없는 경우
     *  * 1. given: 목업 데이터에 memberId는 두지만 gamestatrepo에는 empty로 처리
     *  * 2. when: getMyTier를 진행하면
     *  * 3. then: MEMBER_GAME_STAT_NOT_FOUND_ERROR가 발생해야 함
     */
    @Test
    @DisplayName("내 티어 조회 실패 - 게임 데이터 없음")
    void getMyTier_Fail_NoGameStat() {
        // given
        String memberId = "test-uuid";
        Member member = createMember(memberId);

        given(memberRepository.findByMemberId(memberId)).willReturn(Optional.of(member));
        given(memberGameStatRepository.findByMember(member)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tierService.getMyTier(memberId))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(BaseErrorCode.MEMBER_GAME_STAT_NOT_FOUND_ERROR);
    }

    /**
     *  * (3) 회원이 존재하지 않는 경우에 티어를 조회하는 경우
     *  * 1. given: memberRepo에 memberId를 목업 데이터로 넣지 않는다.
     *  * 2. when: getMyTier를 호출한다.
     *  * 3. then: MEMBER_NOT_FOUND_ERROR 발생한다
     */
    @Test
    @DisplayName("내 티어 조회 실패 - 사용자 없음")
    void getMyTier_Fail_MemberNotFound() {
        // given
        String memberId = "unknown-uuid";
        given(memberRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tierService.getMyTier(memberId))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(BaseErrorCode.MEMBER_NOT_FOUND_ERROR);
    }


    /**
     * (4) 티어 점수를 갱신하는 스케줄러가 정상 작동하는 경우
     *  * 1. given: 최근 종료된 학습 세션이 있을 때, 분석 결과는 60분이 순 공부 시간이라고 해두고,
     *              기존 티어 점수는 100점이라고 할 때
     *  * 2. when: scheduleTierUpdate를 실행하면
     *  * 3. then: 조회한 시간 범위가 정확히 1시간 전에 있던 세션이어야 하고,
     *             점수 계산이 700점으로 계산되어야 하고,
     *             DB에 700점이 저장되어 있고,
     *             Redis에도 랭킹 점수가 업데이트 되어 있어야 한다.
     */
    @Test
    @DisplayName("티어 점수 갱신 스케줄러 실행 시, 1시간 전 범위를 조회하고 점수를 갱신한다")
    void scheduleTierUpdate_Success() {
        // given
        Member member = createMember("test-id");
        StudySession session = StudySession.builder().member(member).build();
        MemberGameStat stat = createMemberGameStat(member, 100, 1000);

        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        
        given(studySessionRepository.findByEndTimeBetween(any(), any()))
                .willReturn(Collections.singletonList(session));
        given(studyAnalysisService.analyzeSession(any())).willReturn(createAnalysisResult(60));
        given(memberGameStatRepository.findByMember(member)).willReturn(Optional.of(stat));
        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);

        // when
        tierService.scheduleTierUpdate();

        // then
        verify(studySessionRepository).findByEndTimeBetween(timeCaptor.capture(), timeCaptor.capture());
        verifyTimeRange(timeCaptor.getAllValues()); // 1시간 범위 검증

        assertThat(stat.getTierScore()).isEqualTo(700); // 100 + (60*10)
        verify(memberGameStatRepository).save(stat);
        verify(zSetOperations).add(eq("rank:private:season:1"), eq("test-id"), eq(700.0));
    }

    private void verifyTimeRange(List<LocalDateTime> capturedTimes) {
        LocalDateTime startTime = capturedTimes.get(0);
        LocalDateTime endTime = capturedTimes.get(1);
        LocalDateTime now = LocalDateTime.now();

        // assertThat(endTime).isCloseTo(now, offset(2000)); // 2초 오차 허용
        assertThat(ChronoUnit.MINUTES.between(startTime, endTime)).isEqualTo(60);
    }

    private Member createMember(String memberId) {
        return Member.builder()
                .id(1L)
                .memberId(memberId)
                .nickName("TestUser")
                .build();
    }

    private MemberGameStat createMemberGameStat(Member member, int score, int totalTime) {
        return MemberGameStat.builder()
                .member(member)
                .tierScore(score)
                .totalStudyTime(totalTime)
                .build();
    }

    private StudyAnalysisResult createAnalysisResult(int netTime) {
        return StudyAnalysisResult.builder()
                .netStudyTime(netTime)
                .drowsyCount(0)
                .phoneCount(0)
                .awayCount(0)
                .build();
    }
}
