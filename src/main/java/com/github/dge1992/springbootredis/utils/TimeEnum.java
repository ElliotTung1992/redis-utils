package com.github.dge1992.springbootredis.utils;

/**
 * @author dongganen
 * @date 2019/5/14
 * @desc: 时间常量
 */
public enum TimeEnum {

    ONE_MINUTE(1 * 60 * 1000l, "一分钟"),
    ONE_HOUR(60 * TimeEnum.ONE_MINUTE.key, "一小时"),
    ONE_DAY(24 * TimeEnum.ONE_MINUTE.key, "一天"),
    ONE_WEEK(7 * TimeEnum.ONE_DAY.key, "一周"),
    ONE_MONTH(30 * TimeEnum.ONE_DAY.key, "一月");

    private final Long key;
    private final String value;

    TimeEnum(Long key, String value){
        this.key = key;
        this.value = value;
    }

    public static TimeEnum getEnumByKey(String key){
        if(null == key){
            return null;
        }
        for(TimeEnum temp: TimeEnum.values()){
            if(temp.getKey().equals(key)){
                return temp;
            }
        }
        return null;
    }

    public Long getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
