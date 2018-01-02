package com.dongganen.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author  dong
 * @create  2018/1/2 15:53
 * @desc redis 发布者配置文件
 **/
@Configuration
public class PublisherConfig {

    @Bean
    public StringRedisTemplate template(RedisConnectionFactory connectionFactory){
        return new StringRedisTemplate(connectionFactory);
    }
}
