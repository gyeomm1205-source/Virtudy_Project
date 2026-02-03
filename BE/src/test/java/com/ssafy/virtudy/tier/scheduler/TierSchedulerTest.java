package com.ssafy.virtudy.tier.scheduler;

import com.ssafy.virtudy.member.repository.MemberGameStatRepository;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TierSchedulerTest {

    @InjectMocks
    private TierScheduler tierScheduler;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private MemberGameStatRepository memberGameStatRepository;
    @Mock
    private StudySessionRepository studySessionRepository;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;
    @Mock
    private SetOperations<String, Object> setOperations;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("DB Flush: 변경분만큼 DB에 반영하고 Redis에서 차감해야 한다")
    void flushDeltaToDb_Normal() {
        // given
        String memberId = "member1";
        // Mock: Dirty Member가 1명 있음
        given(setOperations.pop(eq("study:dirty_members"), anyLong())).willReturn(List.of(memberId));

        // Mock: Redis에 5분, 2점 쌓여있음
        given(hashOperations.get(anyString(), eq("time_min"))).willReturn("5");
        given(hashOperations.get(anyString(), eq("score_point"))).willReturn("2");

        // when
        tierScheduler.flushDeltaToDb();

        // then
        // 1. DB 반영 호출 확인
        verify(memberGameStatRepository).accumulateStats(memberId, 5, 2);
        verify(studySessionRepository).accumulateTime(memberId, 5);

        // 2. Redis 차감 확인 (초기화=0이 아니라 -5, -2여야 함)
        verify(hashOperations).increment(anyString(), eq("time_min"), eq(-5L)); // int vs long 주의 (여기선 increment가 long을 받겠지만 RedisTemplate 설정에 따라 다름)
        verify(hashOperations).increment(anyString(), eq("score_point"), eq(-2L));
    }

    @Test
    @DisplayName("DB Flush: 더티 멤버지만 값이 0이면 DB 호출을 안 해야 한다")
    void flushDeltaToDb_ZeroValue() {
        // given
        String memberId = "member1";
        given(setOperations.pop(anyString(), anyLong())).willReturn(List.of(memberId));

        // Mock: 값 없음 or 0
        given(hashOperations.get(anyString(), eq("time_min"))).willReturn(null);
        given(hashOperations.get(anyString(), eq("score_point"))).willReturn("0");

        // when
        tierScheduler.flushDeltaToDb();

        // then
        verify(memberGameStatRepository, never()).accumulateStats(anyString(), anyInt(), anyInt());
        verify(studySessionRepository, never()).accumulateTime(anyString(), anyInt());
    }

    @Test
    @DisplayName("DB Flush: 멤버가 없으면 아무 일도 안 함")
    void flushDeltaToDb_NoMember() {
        // given
        given(setOperations.pop(anyString(), anyLong())).willReturn(Collections.emptyList());

        // when
        tierScheduler.flushDeltaToDb();

        // then
        verify(hashOperations, never()).get(anyString(), anyString());
    }

    @Test
    @DisplayName("[시나리오 4] DB 반영 실패 시: Redis 값이 차감되지 않고 유지되어야 한다 (Rollback)")
    void flushDeltaToDb_RollbackOnFailure() {
        // given
        String memberId = "member1";
        // Mock: Dirty Member 존재
        given(setOperations.pop(eq("study:dirty_members"), anyLong())).willReturn(List.of(memberId));

        // Mock: Redis에 데이터 존재
        given(hashOperations.get(anyString(), eq("time_min"))).willReturn("5");
        given(hashOperations.get(anyString(), eq("score_point"))).willReturn("2");

        // Mock: DB 저장 시 예외 발생 (강제 실패 상황)
        doThrow(new RuntimeException("DB Connection Error"))
                .when(memberGameStatRepository).accumulateStats(eq(memberId), anyInt(), anyInt());

        // when
        try {
            tierScheduler.flushDeltaToDb();
        } catch (Exception e) {
            // 예외가 전파되는지 여부는 구현에 따라 다르지만, 여기서는 로직 검증에 집중
        }

        // then
        // 1. DB 반영 시도 확인
        verify(memberGameStatRepository).accumulateStats(eq(memberId), eq(5), eq(2));

        // 2. 중요: DB 실패 시 Redis 차감(increment -val)이 실행되지 않았는지 확인
        // 만약 실행되었다면 데이터가 유실된 것임.
        verify(hashOperations, never()).increment(anyString(), eq("time_min"), anyLong());
        verify(hashOperations, never()).increment(anyString(), eq("score_point"), anyLong());
    }

    @Test
    @DisplayName("[Edge Case 1] DB 성공 후 Redis 차감 실패 시: 중복 반영(Double Spending) 취약점 확인")
    void flushDeltaToDb_RedisIncrementFail_CheckDoubleSpending() {
        // given
        String memberId = "member_double_spend";
        String key = "study:member:" + memberId + ":delta";

        given(setOperations.pop(eq("study:dirty_members"), anyLong())).willReturn(List.of(memberId));

        // Mock: Redis has values
        given(hashOperations.get(eq(key), eq("time_min"))).willReturn("10");
        given(hashOperations.get(eq(key), eq("score_point"))).willReturn("5");

        // DB update succeeds

        // Redis decrement fails
        doThrow(new RuntimeException("Redis Connection Error"))
                .when(hashOperations).increment(eq(key), eq("time_min"), anyLong());

        // when
        try {
            tierScheduler.flushDeltaToDb();
        } catch (Exception e) {
            // Logged inside scheduler
        }

        // then
        // 1. DB reflected
        verify(memberGameStatRepository).accumulateStats(eq(memberId), eq(10), eq(5));
        
        // 2. First increment call failed
        verify(hashOperations).increment(eq(key), eq("time_min"), eq(-10L));
        
        // 3. Second increment (score) should NOT happen because of exception
        verify(hashOperations, never()).increment(eq(key), eq("score_point"), anyLong());

        // 4. Important: Member re-added to dirty set -> will be processed again -> Double Spending!
        verify(setOperations).add("study:dirty_members", memberId);
    }

    @Test
    @DisplayName("[Edge Case 2] 배치 처리 중 일부 실패: 성공한 멤버는 반영되고, 실패한 멤버만 롤백(Dirty Set 재추가)")
    void flushDeltaToDb_PartialBatchFailure() {
        // given
        String m1 = "member1", m2 = "member2", m3 = "member3";
        // Key definitions
        String key1 = "study:member:" + m1 + ":delta";
        String key2 = "study:member:" + m2 + ":delta";
        String key3 = "study:member:" + m3 + ":delta";

        given(setOperations.pop(eq("study:dirty_members"), anyLong())).willReturn(List.of(m1, m2, m3));

        // Mocks for member1
        given(hashOperations.get(eq(key1), eq("time_min"))).willReturn("10");
        given(hashOperations.get(eq(key1), eq("score_point"))).willReturn("0");

        // Mocks for member2
        given(hashOperations.get(eq(key2), eq("time_min"))).willReturn("20");
        given(hashOperations.get(eq(key2), eq("score_point"))).willReturn("0");

        // Mocks for member3
        given(hashOperations.get(eq(key3), eq("time_min"))).willReturn("30");
        given(hashOperations.get(eq(key3), eq("score_point"))).willReturn("0");

        // DB behavior: m2 fails, others succeed
        doNothing().when(memberGameStatRepository).accumulateStats(eq(m1), anyInt(), anyInt());
        doThrow(new RuntimeException("DB Error m2"))
                .when(memberGameStatRepository).accumulateStats(eq(m2), anyInt(), anyInt());
        doNothing().when(memberGameStatRepository).accumulateStats(eq(m3), anyInt(), anyInt());

        // when
        tierScheduler.flushDeltaToDb();

        // then
        // m1: Success
        verify(memberGameStatRepository).accumulateStats(eq(m1), eq(10), anyInt());
        verify(hashOperations).increment(eq(key1), eq("time_min"), eq(-10L));
        verify(setOperations, never()).add("study:dirty_members", m1);

        // m2: Fail -> Re-added to dirty
        verify(memberGameStatRepository).accumulateStats(eq(m2), eq(20), anyInt()); // attempted
        verify(hashOperations, never()).increment(eq(key2), eq("time_min"), anyLong()); // not decr
        verify(setOperations).add("study:dirty_members", m2); // re-added

        // m3: Success
        verify(memberGameStatRepository).accumulateStats(eq(m3), eq(30), anyInt());
        verify(hashOperations).increment(eq(key3), eq("time_min"), eq(-30L));
        verify(setOperations, never()).add("study:dirty_members", m3);
    }

    @Test
    @DisplayName("[Edge Case 3] Redis 데이터 타입 오류: 숫자가 아닌 값이 들어있을 때 처리")
    void flushDeltaToDb_InvalidRedisData() {
        // given
        String memberId = "member_invalid";
        String key = "study:member:" + memberId + ":delta";

        given(setOperations.pop(eq("study:dirty_members"), anyLong())).willReturn(List.of(memberId));

        // Redis return non-number for time_min, and null/0 for score_point
        given(hashOperations.get(eq(key), eq("time_min"))).willReturn("NotANumber");
        given(hashOperations.get(eq(key), eq("score_point"))).willReturn("0");

        // when
        tierScheduler.flushDeltaToDb();

        // then
        // DB not called
        verify(memberGameStatRepository, never()).accumulateStats(anyString(), anyInt(), anyInt());
        
        // Redis 차감 호출 안 됨
        verify(hashOperations, never()).increment(anyString(), anyString(), anyLong());

        // 예외 처리 후 Dirty Set에 다시 추가되어야 함 (데이터 수정 후 재처리를 위해)
        verify(setOperations).add("study:dirty_members", memberId);
    }
}
