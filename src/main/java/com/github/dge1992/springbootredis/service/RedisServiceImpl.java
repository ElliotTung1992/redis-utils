package com.github.dge1992.springbootredis.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.dge1992.springbootredis.domain.User;
import org.springframework.stereotype.Service;

/**
 * @Author 小眼睛带鱼
 * @Description
 * @Date 2019/8/6
 **/
@Service
public class RedisServiceImpl implements RedisService {

    @Override
    public String findById(Integer id) {
        return "hello world " + id;
    }

    @Override
    public User findByUser(User user) {
        return user;
    }

    @Override
    public User insertUser(User user) {
        return user;
    }

    @Override
    public void deleteUser(String name) {

    }

    @Override
    public String insertUser2(User user) {
        return JSONObject.toJSONString(user);
    }
}
