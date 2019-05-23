package com.github.dge1992.springbootredis.pubsub;

import org.springframework.stereotype.Component;

/**
 * @Author dongganene
 * @Description
 * @Date 2019/5/23
 **/
@Component
public class KeyEventExpiredReceiver {

    public void receiveMessage(String key) {
        System.out.println(key + " 过期了");
    }
}
