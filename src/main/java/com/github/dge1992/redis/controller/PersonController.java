package com.github.dge1992.redis.controller;


import com.github.dge1992.redis.domain.Person;
import com.github.dge1992.redis.service.IPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dong
 * @since 2018-02-08
 */
@Controller
@RequestMapping("/person")
public class PersonController {

    @Autowired
    private IPersonService personService;

    /**
     * 添加用户
     * @return
     */
    @RequestMapping("/insertPerson")
    @ResponseBody
    public Object insertPerson(){
        Person person = new Person();
        person.setUserName("dong");
        person.setAge(12);
        person.setAddress("宁波");
        person.setSex(1);
        personService.insert(person);
        return "success";
    }

    /**
     * 查询用户列表
     * @return
     */
    @RequestMapping("/queryPersonList")
    @ResponseBody
    public Object queryPersonList(){
        List<Person> personList = personService.queryPersonList();
        return personList;
    }
}

