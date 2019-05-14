package com.github.dge1992.springbootredis;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Author dongganen
 * @Description
 * @Date 2019/4/29
 **/
@Configuration
public class RedisCacheConfig extends CachingConfigurerSupport {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        //使用FastJson为默认序列化方式
        GenericFastJsonRedisSerializer fastJsonRedisSerializer = new GenericFastJsonRedisSerializer();
        template.setDefaultSerializer(fastJsonRedisSerializer);
        template.setValueSerializer(fastJsonRedisSerializer);
        //使用StringRedisSerializer为key的序列化方式
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(fastJsonRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.afterPropertiesSet();
        //开启事务
        template.setEnableTransactionSupport(true);
        return template;
    }

}
