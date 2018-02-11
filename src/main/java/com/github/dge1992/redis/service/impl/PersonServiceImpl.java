package com.github.dge1992.redis.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.dge1992.redis.domain.Person;
import com.github.dge1992.redis.dao.PersonMapper;
import com.github.dge1992.redis.service.IPersonService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dong
 * @since 2018-02-08
 */
@Service
public class PersonServiceImpl extends ServiceImpl<PersonMapper, Person> implements IPersonService {

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ValueOperations<String, Object> stringOperations;

    @Override
    public List<Person> queryPersonList() {

        Object jsonInRedis = stringOperations.get("personList");
        List<Person> session = null;
        if(jsonInRedis != null){
            session = JSONObject.parseArray(jsonInRedis.toString(), Person.class);
            return session;
        }
        List<Person> personList = personMapper.selectList(new EntityWrapper<Person>());
        String json= JSONObject.toJSONString(personList);
        System.out.println(json);
        stringOperations.set("personList", json);
        redisTemplate.expire("personList", 30000, TimeUnit.SECONDS);
        return personList;
    }
}
