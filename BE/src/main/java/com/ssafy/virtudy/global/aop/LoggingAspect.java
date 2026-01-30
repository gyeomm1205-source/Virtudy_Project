package com.ssafy.virtudy.global.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * execution 하위에 있는 패키지 밑 컨트롤러 인식해서 aop로 잡아버림.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Pointcut("execution(* com.ssafy.virtudy.rank.controller..*.*(..))")
    private void cut(){}

    @Around("cut()")
    public Object aroundLog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        // 메서드 정보 받아오기
        Method method = getMethod(proceedingJoinPoint);

        // 파라미터 받아오기
        Object[] args = proceedingJoinPoint.getArgs();

        if (args.length == 0) log.info("no parameter");

        log.info("======= method name = {} ======= | Args: {}", method.getName(), args);

        try {
            // proceed()를 호출하여 실제 메서드 실행
            Object returnObj = proceedingJoinPoint.proceed();
            String returnType = "";
            String returnValue = "";
            // Null 체크 추가
            if (returnObj != null) {
                returnType = returnObj.getClass().getSimpleName();
                returnValue = returnObj.toString();
            } else {
                returnType = "void or null";
                returnValue = "null";
            }

            long executionTime = System.currentTimeMillis() - start;
            log.info("<<<<< End Method: {} | Duration: {}ms", method.getName(), executionTime);
            // 메서드의 리턴값 로깅
            log.info("return type = {}", returnType);
            log.info("return value = {}", returnValue);

            return returnObj;
        } catch (Exception e) {
            log.error("!!!! Error in Method: {} | Message: {}", method.getName(), e.getMessage());
            throw e;
        }
    }

    private java.lang.reflect.Method getMethod(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature signature = (MethodSignature)  proceedingJoinPoint.getSignature();
        return signature.getMethod();
    }
}
