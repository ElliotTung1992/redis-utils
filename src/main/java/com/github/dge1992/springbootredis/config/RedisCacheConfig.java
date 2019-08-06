package com.github.dge1992.springbootredis.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.github.dge1992.springbootredis.pubsub.KeyEventExpiredReceiver;
import com.github.dge1992.springbootredis.pubsub.MessageAdapterReceiver;
import com.github.dge1992.springbootredis.pubsub.MessageReceiver;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Author dongganen
 * @Description
 * @Date 2019/4/29
 **/
@Configuration
public class RedisCacheConfig extends CachingConfigurerSupport {

    /**
     * @author dongganen
     * @date 2019/8/6
     * @desc: 设置RedisTemplate的序列化方式
     */
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

    /**
     * @author dongganen
     * @date 2019/8/6
     * @desc: RedisCacheConfiguration类会判断是否存在RedisCacheConfiguration，不存在创建默认的配置，默认配置使用jdk序列化方式
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        RedisSerializer serializer = new FastJsonRedisSerializer(Object.class);
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        RedisCacheConfiguration config = RedisCacheConfiguration
                .defaultCacheConfig();
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(serializer));
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageReceiver messageReceiver,
                                            MessageListenerAdapter listenerAdapter,
                                            MessageListenerAdapter keyEventExpiredAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        //订阅
        container.addMessageListener(messageReceiver, new PatternTopic("hello*"));
        //使用适配器
        container.addMessageListener(listenerAdapter,  new PatternTopic("hi*"));
        container.addMessageListener(keyEventExpiredAdapter,  new PatternTopic("__keyevent@0__:expired"));
        return container;
    }

    @Bean("listenerAdapter")
    MessageListenerAdapter listenerAdapter(MessageAdapterReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean("keyEventExpiredAdapter")
    MessageListenerAdapter keyEventExpiredAdapter(KeyEventExpiredReceiver keyEventExpiredReceiver) {
        return new MessageListenerAdapter(keyEventExpiredReceiver, "receiveMessage");
    }

}
