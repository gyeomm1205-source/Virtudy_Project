package com.ssafy.virtudy.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RetryAspect {

    @Around("@annotation(retry)")
    public Object doRetry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        int maxRetry = retry.value(); //
        Exception exceptionHolder = null;

        for (int i = 0; i < maxRetry; i++) {
            try {
                return joinPoint.proceed(); // 성공하면 바로 리턴
            } catch (Exception e) {
                exceptionHolder = e;
                log.warn("Retrying... {}/{} error: {}", i + 1, maxRetry, e.getMessage());
                Thread.sleep(100);
            }
        }
        throw exceptionHolder; // 끝까지 실패하면 에러 던짐
    }
}
