package com.ssafy.virtudy.global.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int time() default 1; // 제한 시간 (초 단위, 기본 1초)
    String key() default ""; // 구분용 키 (비워두면 메서드 이름 사용)
}
