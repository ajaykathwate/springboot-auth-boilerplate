package com.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final AppProperties appProperties;

    private String redisHost = appProperties.getRedis().getHost();
    private Integer redisPort = appProperties.getRedis().getPort();

}
