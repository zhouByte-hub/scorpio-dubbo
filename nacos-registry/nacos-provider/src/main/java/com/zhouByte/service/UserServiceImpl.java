package com.zhouByte.service;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户服务基础实现
 */
@DubboService
public class UserServiceImpl implements UserService {

    @Override
    public String userLogin(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        return username + "登录成功";
    }

}
