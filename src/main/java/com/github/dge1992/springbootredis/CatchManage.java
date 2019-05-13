package com.github.dge1992.springbootredis;

import java.util.List;
import java.util.Map;

public interface CatchManage {

    void expire(String key, long timeout);

    <V> void set(String key, V value, long timeout);

    long incrementHash(String key, String hashValue, Long increment);

    List diffMap(Map<String, Object> map1, Map<String, Object> map2);

    List interMap(Map<String, Object> map1, Map<String, Object> map2);

    List unionMap(Map<String, Object> map1, Map<String, Object> map2);
}
