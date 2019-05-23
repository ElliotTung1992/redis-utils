package com.github.dge1992.springbootredis.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.github.dge1992.springbootredis.pubsub.KeyEventExpiredReceiver;
import com.github.dge1992.springbootredis.pubsub.MessageAdapterReceiver;
import com.github.dge1992.springbootredis.pubsub.MessageReceiver;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
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
