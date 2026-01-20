package com.ssafy.virtudy.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration(enforceUniqueMethods = false)
@EnableCaching
public class RedisConfig {
    @Value("${spring.data.redis.port")
    private int port;

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.password:}")
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
}
