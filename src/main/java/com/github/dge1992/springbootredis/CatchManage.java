package com.github.dge1992.springbootredis;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CatchManage {

    /******key******/
    void expire(String key, long timeout);

    <V> void set(String key, V value);

    <V> void set(String key, V value, long timeout);

    <V> void mset(Map<String, V> map);

    Set scan(String pattern, Integer count);

    /******hash******/
    void hmset(String key, Map<String, Object> map);

    long createPrimaryKey(String key, String hashValue, Long increment);

    Set hscan(String key, String pattern, Integer count);

    /******set******/
    List diffMap(Map<String, Object> map1, Map<String, Object> map2);

    List interMap(Map<String, Object> map1, Map<String, Object> map2);

    List unionMap(Map<String, Object> map1, Map<String, Object> map2);

}
