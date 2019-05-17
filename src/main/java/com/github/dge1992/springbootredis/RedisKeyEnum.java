package com.github.dge1992.springbootredis;

/**
 * @author dongganen
 * @date 2019/5/14
 * @desc: Redis常量
 */
public enum RedisKeyEnum {
    COMPANY_NAME("company_name:", "机构名"),
    DEPARTMENT_SOURCE("department_source", "部门数据");
    private final String key;
    private final String value;

    RedisKeyEnum(String key, String value){
        this.key = key;
        this.value = value;
    }

    public static RedisKeyEnum getEnumByKey(String key){
        if(null == key){
            return null;
        }
        for(RedisKeyEnum temp: RedisKeyEnum.values()){
            if(temp.getKey().equals(key)){
                return temp;
            }
        }
        return null;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
