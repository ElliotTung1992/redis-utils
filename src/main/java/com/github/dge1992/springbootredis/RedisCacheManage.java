package com.github.dge1992.springbootredis;

import com.github.dge1992.springbootredis.utils.CommonUtils;
import com.github.dge1992.springbootredis.utils.TimeEnum;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RedisCacheManage implements CacheManage {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void expireKey(String key, long timeout){
        redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void multiSet(Map<String, Object> map){
        redisTemplate.opsForValue().multiSet(map);
    }

    @Override
    public Set getKeyByLike(String pattern, Integer count){
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

    @Override
    public boolean exists(String key) {
        return (Boolean) redisTemplate.execute((RedisCallback) (connection) -> {
            Boolean exists = connection.exists(key.getBytes());
            return exists;
        });
    }

    @Override
    public Long leftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    @Override
    public Long leftPushAll(String key, Object... value) {
        return redisTemplate.opsForList().leftPushAll(key,value);
    }

    @Override
    public Object leftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    @Override
    public Object leftPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().leftPop(key, timeout, unit);
    }

    @Override
    public Long rightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public Long rightPushAll(String key, Object... value) {
        return redisTemplate.opsForList().rightPushAll(key, value);
    }

    @Override
    public Object rightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    @Override
    public void batchInsertHash(String key, Map<String, Object> map){
        redisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public long createPrimaryKey(String key, String hashValue, Long increment){
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

    @Override
    public Set getKeyByLikeHash(String key, String pattern, Integer count){
        Integer mark = Optional.ofNullable(count).orElse(0);
        Set<Object> set = (Set<Object>)redisTemplate.execute((RedisCallback) (connection) -> {
            Set<Object> binaryKeys = new HashSet<>();
            Cursor<Map.Entry<byte[], byte[]>> cursor = connection.hScan(key.getBytes(), new ScanOptions.ScanOptionsBuilder().match(pattern).count(10000).build());
            if(mark == 0){
                while (cursor.hasNext()) {
                    binaryKeys.add(new String(cursor.next().getValue()));
                }
                return binaryKeys;
            }
            IntStream.range(0, count).forEach(e -> {
                if(cursor.hasNext()){
                    binaryKeys.add(new String(cursor.next().getValue()));
                }
            });
            return binaryKeys;
        });
        return set;
    }

    public String[] executeMapCommon(Map<String, Object> map1, Map<String, Object> map2){
        String diffKeyOne = CommonUtils.getUUID();
        String diffKeyTwo = CommonUtils.getUUID();
        map1.entrySet().stream().forEach(e -> redisTemplate.opsForSet().add(diffKeyOne,e.getKey() + ":" + e.getValue()));
        map2.entrySet().stream().forEach(e -> redisTemplate.opsForSet().add(diffKeyTwo,e.getKey() + ":" + e.getValue()));
        this.expireKey(diffKeyOne, TimeEnum.ONE_MINUTE.getKey());
        this.expireKey(diffKeyTwo, TimeEnum.ONE_MINUTE.getKey());
        return new String[]{diffKeyOne, diffKeyTwo};
    }

    @Override
    public List diffMap(Map<String, Object> map1, Map<String, Object> map2){
        String[] keys = executeMapCommon(map1, map2);
        Set difference = redisTemplate.opsForSet().difference(keys[0], keys[1]);
        return (List) difference.stream().map(e -> e.toString().split(":")[0]).collect(Collectors.toList());
    }

    @Override
    public List interMap(Map<String, Object> map1, Map<String, Object> map2){
        String[] keys = executeMapCommon(map1, map2);
        Set inter = redisTemplate.opsForSet().intersect(keys[0], keys[1]);
        return (List) inter.stream().map(e -> e.toString().split(":")[0]).collect(Collectors.toList());
    }

    @Override
    public List unionMap(Map<String, Object> map1, Map<String, Object> map2){
        String[] keys = executeMapCommon(map1, map2);
        Set union = redisTemplate.opsForSet().union(keys[0], keys[1]);
        return (List) union.stream().map(e -> e.toString().split(":")[0]).collect(Collectors.toList());
    }

    @Override
    public void multi(){
        redisTemplate.multi();
    }

    @Override
    public List exec(){
        return redisTemplate.exec();
    }

    public void watch(Object key){
        redisTemplate.watch(key);
    }

}
