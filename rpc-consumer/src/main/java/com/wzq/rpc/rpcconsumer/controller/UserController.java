package com.wzq.rpc.rpcconsumer.controller;

import com.wzq.rpc.api.IUserService;
import com.wzq.rpc.pojo.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * 用户的Controller类
 *
 * @author wzq
 * @create 2022-11-28 14:52
 */
@RestController
@RequestMapping("/user")
public class UserController {

    IUserService userService;

    @RequestMapping("/getUserById")
    public User getUserById(int id) {
        return userService.getById(id);
    }

}
