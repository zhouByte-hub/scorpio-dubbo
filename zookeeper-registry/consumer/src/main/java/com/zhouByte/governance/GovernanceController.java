package com.zhouByte.governance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务治理能力测试
 * 演示版本路由、标签路由、条件路由、分组路由
 * 
 * Dubbo 服务治理说明:
 * @DubboReference 配置:
 *   - version: 服务版本号
 *     - 精确匹配: version="1.0.0" 只调用 V1.0.0 版本
 *     - 通配符匹配: version="*" 可调用任意版本(由注册中心决定)
 *   - group: 服务分组
 *     - 用于服务逻辑隔离，如多租户、业务模块划分
 *     - Consumer 和 Provider 的 group 必须匹配才能调用
 * 
 * 路由规则:
 *   1. 版本路由: 根据 version 匹配不同版本的服务
 *   2. 标签路由: 根据 tag 参数路由到特定实例(如 canary)
 *   3. 条件路由: 根据 Consumer/Provider 属性进行路由
 *   4. 分组路由: 根据 group 进行服务隔离
 */
@RestController
@RequestMapping("/governance")
public class GovernanceController {

    /**
     * 引用 V1.0.0 版本服务
     * @DubboReference 配置:
     *   - version = "1.0.0": 精确匹配 V1 版本
     *   - group = "governance-version": 匹配治理分组
     */
    @DubboReference(
            interfaceClass = UserService.class,
            version = "1.0.0",
            group = "governance-version"
    )
    private UserService v1UserService;

    /**
     * 引用 V2.0.0 版本服务
     */
    @DubboReference(
            interfaceClass = UserService.class,
            version = "2.0.0",
            group = "governance-version"
    )
    private UserService v2UserService;

    /**
     * 引用任意版本服务(通配符)
     * @DubboReference 配置:
     *   - version = "*": 通配符，匹配任意版本
     */
    @DubboReference(
            interfaceClass = UserService.class,
            version = "*",
            group = "governance-version"
    )
    private UserService anyVersionUserService;

    @GetMapping("/version/v1/{username}/{password}")
    public String testVersionV1(@PathVariable String username, @PathVariable String password) {
        return "[版本路由] 调用 V1.0.0 服务:\n" + v1UserService.userLogin(username, password);
    }

    @GetMapping("/version/v2/{username}/{password}")
    public String testVersionV2(@PathVariable String username, @PathVariable String password) {
        return "[版本路由] 调用 V2.0.0 服务:\n" + v2UserService.userLogin(username, password);
    }

    @GetMapping("/version/any/{username}/{password}")
    public String testAnyVersion(@PathVariable String username, @PathVariable String password) {
        return "[版本路由] version='*' 匹配任意版本:\n" + anyVersionUserService.userLogin(username, password);
    }

    @GetMapping("/tag/canary/{username}/{password}")
    public String testTagRouter(@PathVariable String username, @PathVariable String password) {
        try {
            return "[标签路由] 请求带 canary 标签的服务:\n"
                    + anyVersionUserService.userLogin(username, password);
        } catch (Exception e) {
            return "[标签路由] 未找到匹配的标签服务: " + e.getMessage()
                    + "\n提示: 请在 Nacos 配置 tag 规则或使用 Consumer 端参数: tag=canary";
        }
    }

    @GetMapping("/condition/{username}/{password}")
    public String testConditionRouter(@PathVariable String username, @PathVariable String password) {
        return """
                [条件路由演示]
                
                条件路由规则示例 (在 Nacos 中配置):
                consumer应用 == nacos-consumer => provider应用 != nacos-provider-test
                
                常见场景: 黑白名单、机房隔离、环境隔离、读写分离
                
                实际调用结果:
                """ + anyVersionUserService.userLogin(username, password);
    }

    @GetMapping("/group/demo/{username}/{password}")
    public String testGroupRouting(@PathVariable String username, @PathVariable String password) {
        return """
                [分组路由演示]
                
                Group: governance-version
                分组用于服务逻辑隔离，如多租户、业务模块划分、环境区分
                
                调用结果:
                """ + anyVersionUserService.userLogin(username, password);
    }
}
