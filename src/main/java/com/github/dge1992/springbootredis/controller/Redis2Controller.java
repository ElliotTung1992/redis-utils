package com.github.dge1992.springbootredis.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.dge1992.springbootredis.domain.User;
import com.github.dge1992.springbootredis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.web.bind.annotation.*;

/**
 * @Author 小眼睛带鱼
 * @Description
 * @Date 2019/8/6
 **/
@CacheConfig(cacheNames = "user")//省略value＝“xxx”的属性
@RestController
public class Redis2Controller {

    @Autowired
    private RedisService redisService;

    @Cacheable(key="#p0.name", condition = "#p0.age>10")
    @PostMapping("/findByUser2")
    public User findByUser(@RequestBody User user){
        return redisService.findByUser(user);
    }

    @PostMapping("/insertUser2")
    public User insertUser(@RequestBody User user){
        String s = redisService.insertUser2(user);
        return JSONObject.parseObject(s, User.class);
    }

    @CacheEvict(key="#name", allEntries=true)
    @GetMapping("/deleteUser2")
    public void deleteUser(@RequestParam(value = "name", required = false) String name){
        redisService.deleteUser(name);
    }

    /**
     * @author dongganen
     * @date 2019/8/6
     * @desc: 组合多个
     */
    @Caching(put = {
            @CachePut(value = "user", key = "#user.id"),
            @CachePut(value = "user", key = "#user.username"),
            @CachePut(value = "user", key = "#user.email")
    })
    public User save(User user) {
        return null;
    }
}
