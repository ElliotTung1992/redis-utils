package com.github.dge1992.springbootredis.pubsub;

import org.springframework.stereotype.Component;

/**
 * @Author dongganen
 * @Description 消息处理器
 * @Date 2019/5/23
 **/
@Component
public class MessageAdapterReceiver {

    public void receiveMessage(String message) {
        System.out.println("收到一条了消息："+message);
    }
}
