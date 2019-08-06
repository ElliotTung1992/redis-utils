package com.github.dge1992.springbootredis.service;

import com.github.dge1992.springbootredis.domain.User;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;

@CacheConfig(cacheNames = "user")
public interface RedisService {

    String findById(Integer id);

    User findByUser(User user);

    User insertUser(User user);

    void deleteUser(String name);

    @CachePut(key="#p0.name", condition = "#p0.age>10")
    String insertUser2(User user);
}
