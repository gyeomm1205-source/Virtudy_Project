package com.ssafy.virtudy.global.aop;

import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final RedisTemplate<String, String> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 1. 요청자의 IP 혹은 ID 가져오기
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String identifier = attributes.getRequest().getRemoteAddr();
        // JWT를 쓴다면 토큰에서 memberId를 꺼내 쓰는 것이 더 정확합니다.

        // 2. Redis Key 생성 (rate_limit:메서드명:IP)
        String methodName = joinPoint.getSignature().getName();
        String redisKey = "rate_limit:" + methodName + ":" + identifier;

        // 3. Redis 확인
        if (redisTemplate.hasKey(redisKey)) {
            // 이미 키가 있으면 -> 제한 시간 내 재요청임.
            throw new BaseException(BaseErrorCode.TOO_MANY_REQUEST_ERROR);
            //
        }

        // 4. Redis 저장 (값은 의미 없음, 유효ㅗ시간 설정이 핵심)
        redisTemplate.opsForValue().set(redisKey, "1", rateLimit.time(), TimeUnit.SECONDS);

        return joinPoint.proceed();
    }
}
