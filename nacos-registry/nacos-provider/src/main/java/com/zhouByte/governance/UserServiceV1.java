package com.zhouByte.governance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(
        interfaceClass = UserService.class,
        version = "1.0.0",
        group = "governance-version"
)
public class UserServiceV1 implements UserService {

    @Override
    public String userLogin(String username, String password) {
        return "[V1.0.0] 用户登录 - 基础版本\n"
                + "- 使用 MD5 加密密码\n"
                + "- 简单的用户名验证";
    }
}
