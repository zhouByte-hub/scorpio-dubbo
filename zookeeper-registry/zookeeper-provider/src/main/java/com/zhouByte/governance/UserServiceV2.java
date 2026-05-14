package com.zhouByte.governance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户服务 V2.0.0 - 升级版本
 * 升级为 BCrypt 加密，支持多因素认证，添加登录日志记录
 * 
 * Dubbo 版本路由说明:
 * @DubboService 配置:
 *   - interfaceClass = UserService.class: 指定服务接口类型
 *   - version = "2.0.0": 服务版本号，Consumer 通过 version="2.0.0" 精确调用此版本
 *   - group = "governance-version": 服务分组，与 V1/V3 同组，便于统一管理
 */
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
