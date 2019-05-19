package com.github.dge1992.springbootredis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Author dge1992
 * @Description Redis缓存管理类
 * @Date 2019/5/6
 **/
@Component
public class RedisCatchManage implements CatchManage{

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @author dongganen
     * @date 2019/5/9
     * @desc: 设置key的过期时间
     */
    @Override
    public void expire(String key, long timeout){
        redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * @param key
     * @param value
     * @param <V>
     */
    @Override
    public <V> void set(String key, V value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * @author dongganen
     * @date 2019/5/9
     * @desc: 新增key并设置过期时间
     */
    @Override
    public <V> void set(String key, V value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * @author dongganen
     * @date 2019/5/7
     * @desc: 生成唯一key
     */
    @Override
    public long incrementHash(String key, String hashValue, Long increment){
        try {
            increment = Optional.ofNullable(increment).orElse(1l);
            return redisTemplate.opsForHash().increment(key, hashValue, increment);
        }catch (Exception e){
            int first = new Random(10).nextInt(8) + 1;
            int randNo= UUID.randomUUID().toString().hashCode();
            if (randNo < 0) {
                randNo=-randNo;
            }
            return first + Long.valueOf(String.format("%16d", randNo).trim());
        }
    }

    /**
     * @author dongganen
     * @date 2019/5/13
     * @desc: set数据类型 交集 差集 并集公共部分
     */
    public String[] setMapCommon(Map<String, Object> map1, Map<String, Object> map2){
        String diffKeyOne = CommonUils.getUUID();
        String diffKeyTwo = CommonUils.getUUID();
        map1.entrySet().stream().forEach(e -> redisTemplate.opsForSet().add(diffKeyOne,e.getKey() + ":" + e.getValue()));
        map2.entrySet().stream().forEach(e -> redisTemplate.opsForSet().add(diffKeyTwo,e.getKey() + ":" + e.getValue()));
        this.expire(diffKeyOne, TimeEnum.ONE_MINUTE.getKey());
        this.expire(diffKeyTwo, TimeEnum.ONE_MINUTE.getKey());
        return new String[]{diffKeyOne, diffKeyTwo};
    }

    /**
     * @author dongganen
     * @date 2019/5/8
     * @desc: 比对两个map取差集,返回差集的key
     */
    @Override
    public List diffMap(Map<String, Object> map1, Map<String, Object> map2){
        String[] keys = setMapCommon(map1, map2);
        Set difference = redisTemplate.opsForSet().difference(keys[0], keys[1]);
        return (List) difference.stream().map(e -> e.toString().split(":")[0]).collect(Collectors.toList());
    }

    /**
     * @author dongganen
     * @date 2019/5/8
     * @desc: 比对两个map取交集,返回交集的key
     */
    @Override
    public List interMap(Map<String, Object> map1, Map<String, Object> map2){
        String[] keys = setMapCommon(map1, map2);
        Set inter = redisTemplate.opsForSet().intersect(keys[0], keys[1]);
        return (List) inter.stream().map(e -> e.toString().split(":")[0]).collect(Collectors.toList());
    }

    /**
     * @author dongganen
     * @date 2019/5/10
     * @desc: 比对两个map取并集,返回交集的key
     */
    @Override
    public List unionMap(Map<String, Object> map1, Map<String, Object> map2){
        String[] keys = setMapCommon(map1, map2);
        Set union = redisTemplate.opsForSet().union(keys[0], keys[1]);
        return (List) union.stream().map(e -> e.toString().split(":")[0]).collect(Collectors.toList());
    }

    /**
     * @param pattern
     * @param count
     * @return
     * @desc: key模糊查询 + 分页
     */
    @Override
    public Set scan(String pattern, Integer count){
        Integer mark = Optional.ofNullable(count).orElse(0);
        Set<Object> set = (Set<Object>)redisTemplate.execute((RedisCallback) (connection) -> {
            Set<Object> binaryKeys = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan( new ScanOptions.ScanOptionsBuilder().match(pattern).count(1000).build());
            if(mark == 0){
                while (cursor.hasNext()) {
                    binaryKeys.add(new String(cursor.next()));
                }
                return binaryKeys;
            }
            IntStream.range(0, count).forEach(e -> {
                if(cursor.hasNext()){
                    binaryKeys.add(new String(cursor.next()));
                }
            });
            return binaryKeys;
        });
        return set;
    }

}
