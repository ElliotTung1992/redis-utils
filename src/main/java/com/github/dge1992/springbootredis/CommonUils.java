package com.github.dge1992.springbootredis;

import java.util.UUID;

/**
 * @Author dongganene
 * @Description 公共方法工具
 * @Date 2019/5/8
 **/
public class CommonUils {

    /**
     * @author dongganen
     * @date 2019/5/8
     * @desc: 获取一个UUID
     */
    public static String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
