package com.ssafy.virtudy.study.service;

import com.ssafy.virtudy.study.domain.StudyEventType;
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
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyStateHelperTest {

    @InjectMocks
    private StudyStateHelper studyStateHelper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @BeforeEach
    void setUp() {
        // RedisTemplate Operation Mocks
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("최초 상태 업데이트: 버퍼링 없이 상태만 저장되어야 한다")
    void updateState_FirstTime() {
        // given
        String memberId = "member1";
        StudyEventType type = StudyEventType.FOCUS;
        LocalDateTime now = LocalDateTime.now();

        // Mock: 이전 상태 없음 (null)
        given(hashOperations.get(anyString(), eq("last_type"))).willReturn(null);
        given(hashOperations.get(anyString(), eq("last_time"))).willReturn(null);

        // when
        studyStateHelper.updateState(memberId, type, now);

        // then
        // 1. 상태 저장 호출 확인
        verify(hashOperations).putAll(anyString(), anyMap());
        // 2. 버퍼 증가(increment)는 호출되지 않아야 함
        verify(hashOperations, never()).increment(anyString(), eq("time_buffer"), anyLong());
    }

    @Test
    @DisplayName("[시나리오 1-A] 59초 공부: 1분이 안 되면 buffer만 59로 쌓여야 한다")
    void updateState_AccumulateBuffer() {
        // given
        String memberId = "member1";
        LocalDateTime lastTime = LocalDateTime.now().minusSeconds(59); // 59초 전
        LocalDateTime now = LocalDateTime.now();
        String key = "study:member:" + memberId + ":delta";

        // Mock: 이전 상태 FOCUS, 59초 전
        given(hashOperations.get(anyString(), eq("last_type"))).willReturn("FOCUS");
        given(hashOperations.get(anyString(), eq("last_time"))).willReturn(lastTime.toString());

        // Mock: 현재 buffer는 0 -> 59 증가
        given(hashOperations.increment(anyString(), eq("time_buffer"), eq(59L))).willReturn(59L);
        given(hashOperations.increment(anyString(), eq("score_buffer"), eq(59L))).willReturn(59L);

        // when
        studyStateHelper.updateState(memberId, StudyEventType.FOCUS, now);

        // then
        // 1. 버퍼 증가 호출 확인 (59초)
        verify(hashOperations).increment(key, "time_buffer", 59L);
        // 2. 분 단위 승격(time_min)은 없어야 함 (매우 중요)
        verify(hashOperations, never()).increment(key, "time_min", 1L);
    }
    
    @Test
    @DisplayName("[시나리오 1-B] 60초 공부: 딱 1분이 되면 time_min이 1 증가해야 한다")
    void updateState_PromoteTimeMin_ExactBoundary() {
        // given
        String memberId = "member1";
        LocalDateTime lastTime = LocalDateTime.now().minusSeconds(60); // 딱 60초 전
        LocalDateTime now = LocalDateTime.now();
        String key = "study:member:" + memberId + ":delta";

        // Mock: 이전 상태 FOCUS
        given(hashOperations.get(anyString(), eq("last_type"))).willReturn("FOCUS");
        given(hashOperations.get(anyString(), eq("last_time"))).willReturn(lastTime.toString());

        // Mock: time_buffer 증가 시 60 반환 (0 -> 60)
        given(hashOperations.increment(anyString(), eq("time_buffer"), eq(60L))).willReturn(60L);
        given(hashOperations.increment(anyString(), eq("score_buffer"), eq(60L))).willReturn(60L);

        // [Fix] time_min 증가에 대한 Stubbing 추가
        given(hashOperations.increment(anyString(), eq("time_min"), anyLong())).willReturn(1L);

        // when
        studyStateHelper.updateState(memberId, StudyEventType.FOCUS, now);

        // then
        // 1. time_min 1 증가 확인 (60 / 60 = 1)
        verify(hashOperations).increment(anyString(), eq("time_min"), eq(1L));
        // 2. time_buffer 0으로 초기화 확인 (60 % 60 = 0)
        verify(hashOperations).put(anyString(), eq("time_buffer"), eq(0L));
    }

    @Test
    @DisplayName("시간 승격: 1분이 넘으면 time_min이 증가해야 한다")
    void updateState_PromoteTimeMin() {
        // given
        String memberId = "member1";
        String key = "study:member:" + memberId + ":delta";
        LocalDateTime lastTime = LocalDateTime.now().minusSeconds(40);

        given(hashOperations.get(key, "last_type")).willReturn("FOCUS");
        given(hashOperations.get(key, "last_time")).willReturn(lastTime.toString());

        // 1. time_buffer 증가 (40초 유입 -> 총 75초 가정)
        given(hashOperations.increment(key, "time_buffer", 40L)).willReturn(75L);
        // 2. [추가] time_min 1분 증가에 대한 Stubbing
        given(hashOperations.increment(key, "time_min", 1L)).willReturn(1L);
        // 3. score_buffer 증가 (75초)
        given(hashOperations.increment(key, "score_buffer", 40L)).willReturn(75L);

        // when
        studyStateHelper.updateState(memberId, StudyEventType.FOCUS, LocalDateTime.now());

        // then
        verify(hashOperations).increment(key, "time_min", 1L); // 75 / 60 = 1
        verify(hashOperations).put(key, "time_buffer", 15L);    // 75 % 60 = 15
        verify(setOperations).add("study:dirty_members", memberId);
    }
//    @Test
//    @DisplayName("시간 승격: 1분이 넘으면 time_min이 증가해야 한다")
//    void updateState_PromoteTimeMin() {
//        // given
//        String memberId = "member1";
//        String key = "study:member:" + memberId + ":delta";
//        LocalDateTime lastTime = LocalDateTime.now().minusSeconds(40);
//
//        given(hashOperations.get(key, "last_type")).willReturn("FOCUS");
//        given(hashOperations.get(key, "last_time")).willReturn(lastTime.toString());
//
//        // 1. time_buffer 증가 (40초 유입 -> 총 75초 가정)
//        given(hashOperations.increment(key, "time_buffer", 40L)).willReturn(75L);
//        // 2. [추가] time_min 1분 증가에 대한 Stubbing
//        given(hashOperations.increment(key, "time_min", 1L)).willReturn(1L);
//        // 3. score_buffer 증가 (75초)
//        given(hashOperations.increment(key, "score_buffer", 40L)).willReturn(75L);
//
//        // when
//        studyStateHelper.updateState(memberId, StudyEventType.FOCUS, LocalDateTime.now());
//
//        // then
//        verify(hashOperations).increment(key, "time_min", 1L); // 75 / 60 = 1
//        verify(hashOperations).put(key, "time_buffer", 15L);    // 75 % 60 = 15
//        verify(setOperations).add("study:dirty_members", memberId);
//    }

    @Test
    @DisplayName("[시나리오 2-B] 10분 공부: 점수 승격 및 버퍼 초기화 확인")
    void updateState_PromoteScorePoint() {
        // given
        String memberId = "member1";
        String key = "study:member:" + memberId + ":delta";
        LocalDateTime lastTime = LocalDateTime.now().minusSeconds(600); // 10분

        given(hashOperations.get(key, "last_type")).willReturn("FOCUS");
        given(hashOperations.get(key, "last_time")).willReturn(lastTime.toString());

        // 10분(600초)이 경과했을 때의 Stubbing들
        given(hashOperations.increment(key, "time_buffer", 600L)).willReturn(600L);
        given(hashOperations.increment(key, "time_min", 10L)).willReturn(10L); // 10분 추가됨

        given(hashOperations.increment(key, "score_buffer", 600L)).willReturn(600L);
        given(hashOperations.increment(key, "score_point", 1L)).willReturn(1L); // 점수 1점 추가됨

        // when
        studyStateHelper.updateState(memberId, StudyEventType.FOCUS, LocalDateTime.now());

        // then
        verify(hashOperations).increment(key, "score_point", 1L);
        verify(hashOperations).put(key, "score_buffer", 0L);
    }
//    @Test
//    @DisplayName("[시나리오 2-B] 10분 공부: 점수 승격 및 버퍼 초기화 확인")
//    void updateState_PromoteScorePoint() {
//        // given
//        String memberId = "member1";
//        String key = "study:member:" + memberId + ":delta";
//        LocalDateTime lastTime = LocalDateTime.now().minusSeconds(600); // 10분
//
//        given(hashOperations.get(key, "last_type")).willReturn("FOCUS");
//        given(hashOperations.get(key, "last_time")).willReturn(lastTime.toString());
//
//        // 10분(600초)이 경과했을 때의 Stubbing들
//        given(hashOperations.increment(key, "time_buffer", 600L)).willReturn(600L);
//        given(hashOperations.increment(key, "time_min", 10L)).willReturn(10L); // 10분 추가됨
//
//        given(hashOperations.increment(key, "score_buffer", 600L)).willReturn(600L);
//        given(hashOperations.increment(key, "score_point", 1L)).willReturn(1L); // 점수 1점 추가됨
//
//        // when
//        studyStateHelper.updateState(memberId, StudyEventType.FOCUS, LocalDateTime.now());
//
//        // then
//        verify(hashOperations).increment(key, "score_point", 1L);
//        verify(hashOperations).put(key, "score_buffer", 0L);
//    }

    @Test
    @DisplayName("[시나리오 3] 딴짓(PHONE, SLEEP): 공부 시간이 누적되지 않아야 한다")
    void updateState_IgnoreNonFocus() {
        // given
        String memberId = "member1";
        LocalDateTime lastTime = LocalDateTime.now().minusSeconds(30);
        LocalDateTime now = LocalDateTime.now();

        // Mock: 이전 상태 PHONE (딴짓 중)
        given(hashOperations.get(anyString(), eq("last_type"))).willReturn("PHONE");
        given(hashOperations.get(anyString(), eq("last_time"))).willReturn(lastTime.toString());

        // when
        // 다시 FOCUS로 돌아오든, 계속 PHONE이든, 이전 상태가 PHONE이었으므로 시간 누적 로직을 타면 안 됨.
        studyStateHelper.updateState(memberId, StudyEventType.FOCUS, now);

        // then
        // 1. time_buffer 증가(increment)가 호출되지 않아야 함
        verify(hashOperations, never()).increment(anyString(), eq("time_buffer"), anyLong());
        // 2. score_buffer 증가도 없어야 함
        verify(hashOperations, never()).increment(anyString(), eq("score_buffer"), anyLong());
    }
}
