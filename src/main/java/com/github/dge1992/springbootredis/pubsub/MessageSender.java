package com.github.dge1992.springbootredis.pubsub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author dongganen
 * @Description 消息发送器
 * @Date 2019/5/23
 **/
@Component
public class MessageSender {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void sendMessage(String channel, String message){
        stringRedisTemplate.convertAndSend(channel, message);
    }
}
