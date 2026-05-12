package com.zhouByte.config;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dynamic-config")
public class DynamicConfigController {

    @DubboReference(
            interfaceClass = UserService.class,
            group = "dynamic-config",
            version = "1.0.0",
            check = false,
            methods = {
                    @Method(
                            name = "userLogin",
                            timeout = 5000,
                            retries = 3,
                            loadbalance = "roundrobin"
                    )
            },
            parameters = {
                    "timeout", "5000",
                    "retries", "3"
            }
    )
    private UserService dynamicConfigUserService;

    @GetMapping("/current/{username}/{password}")
    public String testCurrentConfig(@PathVariable String username, @PathVariable String password) {
        return """
                [动态配置 - 当前配置]
                
                Consumer 端配置:
                - timeout: 5000ms
                - retries: 3次
                - loadbalance: roundrobin
                
                调用结果:
                """ + dynamicConfigUserService.userLogin(username, password);
    }

    @GetMapping("/override/demo/{username}/{password}")
    public String testOverrideConfig(@PathVariable String username, @PathVariable String password) {
        return """
                [动态配置覆盖机制]
                
                Dubbo 配置优先级 (从高到低):
                1. JVM -D 参数 (最高)
                2. Consumer/Provider 方法级 @Method 注解
                3. Consumer/Provider 级别 @DubboReference/@DubboService
                4. 全局配置 (application.yaml)
                5. 注册中心动态配置 (Nacos Config)
                
                演示场景:
                在 Nacos 控制台添加以下配置，无需重启即可生效:
                
                ====> 配置示例 <====
                Key: dubbo.consumer.com.zhouByte.api.UserService.timeout
                Value: 8000
                
                或使用 Nacos 的规则配置:
                ====> Override URL <====
                override://0.0.0.0/com.zhouByte.api.UserService?category=configurators&dynamic=false&timeout=10000
                
                当前调用:
                """ + dynamicConfigUserService.userLogin(username, password);
    }

    @GetMapping("/parameters/{username}/{password}")
    public String testCustomParameters(@PathVariable String username, @PathVariable String password) {
        return """
                [自定义参数传递]
                
                除了标准参数外，还可以传递自定义参数:
                - parameters = {"key1": "value1"}
                
                使用场景:
                1. 业务标识透传
                2. 租户 ID 传递
                3. 功能开关控制
                4. A/B 测试标记
                
                Provider 端可通过以下方式获取:
                RpcContext.getContext().getUrl().getParameter("key1")
                
                调用结果:
                """ + dynamicConfigUserService.userLogin(username, password);
    }

    @GetMapping("/method-level/{username}/{password}")
    public String testMethodLevelConfig(@PathVariable String username, @PathVariable String password) {
        return """
                [方法级粒度配置]
                
                同一服务的不同方法可以有不同的配置:
                
                @Method(name="login", timeout=2000)   // 登录接口快响应
                @Method(name="query", timeout=10000)   // 查询接口可慢
                
                优势:
                - 细粒度控制
                - 针对性优化
                - 不同业务场景差异化处理
                
                调用结果:
                """ + dynamicConfigUserService.userLogin(username, password);
    }
}
