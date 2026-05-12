package com.zhouByte.governance.provider.service;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(
        interfaceClass = UserService.class,
        version = "3.0.0",
        group = "governance-version",
        parameters = {"tag", "canary"}
)
public class UserServiceV3Canary implements UserService {

    @Override
    public String userLogin(String username, String password) {
        return "[V3.0.0-CANARY] 金丝雀版本 - 新功能测试中\n"
                + "- 使用 RSA 非对称加密\n"
                + "- 支持生物识别\n"
                + "- AI 风控检测\n"
                + "- 实时行为分析";
    }
}
