package com.zhouByte.governance.consumer.controller;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/governance")
public class GovernanceController {

    @DubboReference(
            interfaceClass = UserService.class,
            version = "1.0.0",
            group = "governance-version"
    )
    private userService v1UserService;

    @DubboReference(
            interfaceClass = UserService.class,
            version = "2.0.0",
            group = "governance-version"
    )
    private userService v2UserService;

    @DubboReference(
            interfaceClass = UserService.class,
            version = "*",
            group = "governance-version"
    )
    private userService anyVersionUserService;

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
                
                当前请求参数:
                - username: %s
                
                条件路由规则示例 (在 Nacos 中配置):
                ====> consumer应用 == nacos-consumer => provider应用 != nacos-provider-test
                
                说明:
                - 当消费者是 nacos-consumer 时
                - 只调用非测试环境的提供者
                - 可用于环境隔离 (dev/test/prod)
                
                实际调用结果:
                """.formatted(username) + anyVersionUserService.userLogin(username, password);
    }

    @GetMapping("/group/demo/{username}/{password}")
    public String testGroupRouting(@PathVariable String username, @PathVariable String password) {
        return """
                [分组路由演示]
                
                Group: governance-version
                
                分组的作用:
                1. 服务逻辑分组 (如: 按业务模块)
                2. 多租户隔离
                3. 不同配置的服务实例
                
                调用结果:
                """ + anyVersionUserService.userLogin(username, password);
    }
}
