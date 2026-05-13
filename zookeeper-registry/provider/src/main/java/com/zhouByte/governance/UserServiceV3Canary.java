package com.zhouByte.governance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户服务 V3.0.0 - 金丝雀版本（Canary）
 * 带 tag=canary 标签，用于金丝雀发布和灰度测试
 * 
 * Dubbo 金丝雀发布说明:
 * @DubboService 配置:
 *   - interfaceClass = UserService.class: 指定服务接口类型
 *   - version = "3.0.0": 金丝雀版本号，代表正在测试的新版本
 *   - group = "governance-version": 与其他版本同组，便于统一管理
 *   - parameters = {"tag", "canary"}: 
 *     自定义标签参数，用于标签路由(Tag Router)
 *     Consumer 可通过设置 tag=canary 将请求路由到此金丝雀实例
 * 
 * 金丝雀发布流程:
 *   1. 新版本以 canary 标签部署，只接收带 canary 标签的流量
 *   2. 验证新版本功能正常后，逐步扩大流量比例
 *   3. 全量验证通过后，将 version 升级为正式版本
 */
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
