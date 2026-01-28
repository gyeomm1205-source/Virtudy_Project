package com.ssafy.virtudy.global.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.Timer; // 이걸로 선택하세요!
import io.micrometer.core.instrument.MeterRegistry;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PerformanceAspect {

    private final MeterRegistry meterRegistry;

    // @LogExecutionTime 어노테이션이 붙은 곳만 타겟팅
    @Around("@annotation(com.ssafy.virtudy.global.aop.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed(); // 실제 메서드 실행

        long executionTime = System.currentTimeMillis() - start;

        // 3초 (3000ms) 이상 걸리면 경고 로그 (WARN)
        if (executionTime > 3000) {
            log.warn("🐢 [SLOW QUERY] Method: {} | Time: {}ms", joinPoint.getSignature(), executionTime);
        } else {
            // 평소에는 정보 로그 (INFO)
            log.info("⚡ [FAST] Method: {} | Time: {}ms", joinPoint.getSignature(), executionTime);
        }

        // --- [추가하신 코드 위치] ---
        Timer.builder("method.execution.time") // 메트릭 이름
                .tag("class", joinPoint.getSignature().getName())           // 어떤 클래스인지 구분
                .tag("method", joinPoint.getSignature().getDeclaringType().getSimpleName())         // 어떤 메서드인지 구분
                .description("Time taken to execute method")
                .register(meterRegistry)           // 레지스트리에 등록
                .record(executionTime, TimeUnit.MILLISECONDS); // 시간 기록
        // -------------------------

        return proceed;
    }
}
