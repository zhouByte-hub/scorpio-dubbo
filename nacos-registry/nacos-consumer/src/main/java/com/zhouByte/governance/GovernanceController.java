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
 */
@RestController
@RequestMapping("/governance")
public class GovernanceController {

    @DubboReference(
            interfaceClass = UserService.class,
            version = "1.0.0",
            group = "governance-version"
    )
    private UserService v1UserService;

    @DubboReference(
            interfaceClass = UserService.class,
            version = "2.0.0",
            group = "governance-version"
    )
    private UserService v2UserService;

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
