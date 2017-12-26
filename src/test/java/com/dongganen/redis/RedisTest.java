package com.dongganen.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    JedisPool jedisPool;

    @Test
    public void test(){
        long now = System.currentTimeMillis() / 1000;
        long l = now / 86400 /365;
        int i = 2017 - 48;
        System.out.println(i);
    }

    @Test
    public void test1(){
        Jedis jedis = jedisPool.getResource();
        jedis.set("aaa", "dong");
    }
}
