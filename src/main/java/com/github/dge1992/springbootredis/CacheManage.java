package com.github.dge1992.springbootredis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface CacheManage {

    /************************key************************/

    /**
     * @author dongganen
     * @date 2019/5/9
     * @desc: 设置key的过期时间
     */
    void expireKey(String key, long timeout);

    /**
     * @author dongganen
     * @date 2019/5/9
     * @desc: 新增key
     */
    void insertValue(String key, Object value);

    /**
     * @author dongganen
     * @date 2019/5/9
     * @desc: 新增key并设置过期时间
     */
    void insertValue(String key, Object value, long timeout);

    /**
     * @author dongganen
     * @date 2019/5/20
     * @desc: 批量导入 数据不宜超过10w
     */
    void batchInsertValue(Map<String, Object> map);

    /**
     * @param pattern
     * @param count
     * @return
     * @desc: key模糊查询 + 分页
     */
    Set getKeyByLike(String pattern, Integer count);

    /**
     * @author dongganen
     * @date 2019/5/22
     * @desc: 查看key是否存在
     */
    boolean existsKey(String key);

    /************************list************************/

    /**
     * @author dongganen
     * @date 2019/5/22
     * @desc: 左边放入
     */
    Long leftPush(String key, Object value);

    /**
     * @author dongganen
     * @date 2019/5/22
     * @desc: 左边全部放入
     */
    Long leftPushAll(String key, Object... value);

    /**
     * @author dongganen
     * @date 2019/5/22
     * @desc: 左边拿出
     */
    Object leftPop(String key);

    /**
     * @author dongganen
     * @date 2019/5/22
     * @desc: 左边拿出
     */
    Object leftPop(String key, long timeout, TimeUnit unit);

    /**
     * @author dongganen
     * @date 2019/5/22
     * @desc: 右边放入
     */
    Long rightPush(String key, Object value);

    /**
     * @author dongganen
     * @date 2019/5/22
     * @desc: 右边全部放入
     */
    Long rightPushAll(String key, Object... value);

    /**
     * @author dongganen
     * @date 2019/5/22
     * @desc: 右边拿出
     */
    Object rightPop(String key);


    /************************hash************************/

    /**
     * @author dongganen
     * @date 2019/5/20
     * @desc: 批量导入hash类型数据
     */
    void batchInsertHash(String key, Map<String, Object> map);

    /**
     * @author dongganen
     * @date 2019/5/7
     * @desc: 生成唯一key
     */
    long createPrimaryKey(String key, String hashValue, Long increment);

    /**
     * @param pattern
     * @param count
     * @return
     * @desc: hash数据key模糊查询 + 分页
     */
    Set getKeyByLikeHash(String key, String pattern, Integer count);

    /************************set************************/

    /**
     * @author dongganen
     * @date 2019/5/8
     * @desc: 比对两个map取差集,返回差集的key
     */
    List diffMap(Map<String, Object> map1, Map<String, Object> map2);

    /**
     * @author dongganen
     * @date 2019/5/8
     * @desc: 比对两个map取交集,返回交集的key
     */
    List interMap(Map<String, Object> map1, Map<String, Object> map2);

    /**
     * @author dongganen
     * @date 2019/5/10
     * @desc: 比对两个map取并集,返回交集的key
     */
    List unionMap(Map<String, Object> map1, Map<String, Object> map2);

}
