package com.github.dge1992.redis.service;

import com.github.dge1992.redis.domain.Person;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dong
 * @since 2018-02-08
 */
public interface IPersonService extends IService<Person> {

    List<Person> queryPersonList();
}
