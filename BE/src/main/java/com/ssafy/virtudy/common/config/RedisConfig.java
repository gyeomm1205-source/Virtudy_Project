package com.ssafy.virtudy.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(enforceUniqueMethods = false)
@EnableCaching
public class RedisConfig {
    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.password}")
    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 여기다가 setPort, host
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setPort(port);
        config.setHostName(host);
        config.setPassword(password);
        return new LettuceConnectionFactory(config);
    }

    /**
     * [핵심 변경]
     * 1. 메서드 이름을 redisTemplate으로 변경 (범용성)
     * 2. @Bean 추가 (스프링에게 "이걸 써!"라고 알려줌)
     * 3. Key, Value 모두 StringSerializer 적용 (외계어 방지)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // 일반적인 Key:Value 저장 시 사람이 읽을 수 있게 직렬화
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        // Hash 자료구조(객체 저장)를 쓸 때를 위한 설정
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        // 모든 설정 적용
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
