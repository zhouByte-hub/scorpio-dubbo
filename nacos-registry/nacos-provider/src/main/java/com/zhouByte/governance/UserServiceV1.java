package com.zhouByte.governance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户服务 V1.0.0 - 基础版本
 * 使用 MD5 加密密码，仅做简单用户名验证
 * 
 * Dubbo 版本路由说明:
 * @DubboService 配置:
 *   - interfaceClass = UserService.class: 指定服务接口类型
 *   - version = "1.0.0": 服务版本号，Consumer 通过 version="1.0.0" 精确调用此版本
 *   - group = "governance-version": 服务分组，将多个版本的服务归为一组进行管理
 * 
 * 版本管理场景:
 *   - 多版本共存: V1/V2/V3 可同时在线运行
 *   - 版本路由: Consumer 指定 version 即可路由到对应版本
 *   - 灰度发布: 新版本先以小流量运行，验证后逐步扩大
 */
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
