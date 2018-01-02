package com.dongganen.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Redis_pub_sub_Test {

    @Autowired
    private StringRedisTemplate template;

    @Autowired
    private CountDownLatch latch;

    @Test
    public void test() throws InterruptedException {

        template.convertAndSend("phone", "Hello from Redis!");

        latch.await();

        System.exit(0);
    }
}
