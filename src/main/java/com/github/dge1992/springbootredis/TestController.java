package com.github.dge1992.springbootredis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @Author dongganene
 * @Description
 * @Date 2019/4/29
 **/
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private CatchManage catchManage;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/test")
    public Object test(){
//        return catchManage.incrementHash(CatchConstant.HASHRANDOMKEY, CatchConstant.ID, 5l);
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("age", "23");
//        map1.put("name", "dge");
//        map1.put("address", "宁波");
//        map1.put("sex", "男");
//        Map<String, Object> map2 = new HashMap<>();
//        map2.put("age", "23");
//        map2.put("name", "fnn");
//        map2.put("address", "上海");
//        map2.put("tel", "110");
//        return catchManage.unionMap(map1, map2);
//        redisTemplate.multi();
//        redisTemplate.opsForValue().set("test", "test");
//        redisTemplate.delete("test");
//        redisTemplate.exec();
//        return "";

        for (int i = 0; i < 1200; i++){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            catchManage.set("" + i, "hahaha", TimeEnum.ONE_MONTH.getKey());
        }
        return "";
    }
}
