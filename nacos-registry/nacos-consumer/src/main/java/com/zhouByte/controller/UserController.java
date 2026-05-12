package com.zhouByte.controller;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @DubboReference(loadbalance = "random", cluster = "failover", retries = 2)
    private UserService userService;

    @GetMapping(value = "/login/{username}/{password}")
    public String userLogin(@PathVariable("username") String username, @PathVariable("password") String password){
        return userService.userLogin(username, password);
    }
}
