package com.github.dge1992.springbootredis.pubsub;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * @Author dongganen
 * @Description 消息处理器
 * @Date 2019/5/23
 **/
@Component
public class MessageReceiver implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println(new String(pattern) + " 频道发布一条了消息："+message);
    }
}
