package com.github.dge1992.springbootredis.controller;

import com.github.dge1992.springbootredis.domain.User;
import com.github.dge1992.springbootredis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

/**
 * @Author 小眼睛带鱼
 * @Description
 * @Date 2019/8/6
 **/
@RestController
public class RedisController {

    @Autowired
    private RedisService redisService;

    /**
     * 主要方法返回值将被加入缓存,
     * 同时在查询时，会先从缓存中获取,
     * 若不存在才再发起对数据库的访问。
     *
     * value: 缓存的名称，在 spring 配置文件中定义，必须指定至少一个
     * key: 缓存的 key，可以为空，如果指定要按照 SpEL 表达式编写，如果不指定，则缺省按照方法的所有参数进行组合
     * condition: 缓存的条件，可以为空，使用 SpEL 编写，返回 true 或者 false，只有为 true 才进行缓存
     *
     * 属性名称      描述                          示例
     * methodName    当前方法名                    #root.methodName
     * method        当前方法                      #root.method.name
     * target        当前被调用的对象              #root.target
     * targetClass   当前被调用的对象的class       #root.targetClass
     * args          当前方法参数组成的数组        #root.args[0]
     * caches        当前被调用的方法使用的Cache   #root.caches[0].name
     *
     */
    //@Cacheable(value = "findById", key="#id", condition = "#id!=null")
    @Cacheable(value = "findById", key="#p0", condition = "#id!=null")
    @GetMapping("/findById")
    public String findById(@RequestParam(value = "id", required = false) Integer id){
        return redisService.findById(id);
    }

    //@Cacheable(value = "findByUser", key="#user.name")
    @Cacheable(value = {"findByUser1", "findByUser2"}, key="#p0.name", condition = "#p0.age>10")
    @PostMapping("/findByUser")
    public User findByUser(@RequestBody User user){
        return redisService.findByUser(user);
    }

    /**
     * 即调用方法,又更新缓存数据;
     * 适合更新和修改
     */
    @CachePut(value = {"findByUser1", "findByUser2"}, key="#p0.name", condition = "#p0.age>10")
    @PostMapping("/insertUser")
    public User insertUser(@RequestBody User user){
        return redisService.insertUser(user);
    }

    /**
     * 缓存清除
     *
     * allEntries          是否清空所有缓存内容，缺省为 false，如果指定为 true，则方法调用后将立即清空所有缓存
     * @CachEvict(value=”testcache”,allEntries=true)
     * beforeInvocation    是否在方法执行前就清空，缺省为 false，如果指定为 true，则在方法还没有执行的时候就清空缓存，缺省情况下，如果方法执行抛出异常，则不会清空缓存
     * @CachEvict(value=”testcache”，beforeInvocation=true)
     */
    @CacheEvict(value = {"findByUser1", "findByUser2"}, key="#name", allEntries=true)
    @GetMapping("/deleteUser")
    public void deleteUser(@RequestParam(value = "name", required = false) String name){
        redisService.deleteUser(name);
    }
}
