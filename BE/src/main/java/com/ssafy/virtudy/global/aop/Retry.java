package com.ssafy.virtudy.global.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 메서드 위에만 붙일 수 있음.
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지됨 (AOP에서 읽기 위해 필수)
public @interface Retry {
    int value() default 3;
}
