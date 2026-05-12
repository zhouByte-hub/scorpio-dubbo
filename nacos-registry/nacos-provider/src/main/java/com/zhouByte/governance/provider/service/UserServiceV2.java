package com.zhouByte.governance.provider.service;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(
        interfaceClass = UserService.class,
        version = "2.0.0",
        group = "governance-version"
)
public class UserServiceV2 implements UserService {

    @Override
    public String userLogin(String username, String password) {
        return "[V2.0.0] 用户登录 - 升级版本\n"
                + "- 使用 BCrypt 加密密码\n"
                + "- 支持多因素认证\n"
                + "- 添加登录日志记录";
    }
}
