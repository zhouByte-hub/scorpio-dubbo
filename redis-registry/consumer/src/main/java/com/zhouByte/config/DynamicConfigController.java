package com.zhouByte.config;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 动态配置测试
 * 演示配置查看、配置覆盖、自定义参数、方法级配置
 * 
 * Dubbo 配置说明:
 * @DubboReference 配置:
 *   - check: 启动时是否检查服务提供者存在
 *     - false: 不检查，启动时不报错，首次调用时才发现
 *     - true(默认): 检查，启动时找不到服务则报错
 *   - methods: 方法级配置数组，使用 @Method 注解
 *     - 可对单个方法设置独立的 timeout、retries、loadbalance 等
 *   - parameters: 自定义参数键值对数组
 *     - 格式: {"key1", "value1", "key2", "value2"}
 *     - Provider 端可通过 RpcContext.getUrl().getParameter("key") 获取
 * 
 * @Method 注解说明:
 *   - name: 方法名，必须与接口中定义的方法名一致
 *   - timeout: 该方法调用超时时间(毫秒)
 *   - retries: 该方法失败重试次数
 *   - loadbalance: 该方法的负载均衡策略
 */
@RestController
@RequestMapping("/dynamic-config")
public class DynamicConfigController {

    /**
     * 引用动态配置服务
     * @DubboReference 配置:
     *   - group = "dynamic-config": 匹配动态配置分组
     *   - version = "1.0.0": 匹配版本号
     *   - check = false: 启动时不检查服务是否存在
     *   - methods: 方法级配置，userLogin 方法超时 5 秒、重试 3 次、轮询策略
     *   - parameters: 自定义参数 timeout=5000, retries=3
     */
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
                1. JVM -D 参数
                2. 方法级 @Method 注解
                3. 接口级 @DubboReference 注解
                4. 全局配置 (application.yaml)
                5. 注册中心动态配置 (Nacos Config)
                
                当前调用:
                """ + dynamicConfigUserService.userLogin(username, password);
    }

    @GetMapping("/parameters/{username}/{password}")
    public String testCustomParameters(@PathVariable String username, @PathVariable String password) {
        return """
                [自定义参数传递]
                
                通过 parameters = {"key": "value"} 传递自定义参数
                Provider 端通过 RpcContext.getUrl().getParameter("key") 获取
                
                使用场景: 业务标识透传、租户ID、功能开关、A/B测试
                
                调用结果:
                """ + dynamicConfigUserService.userLogin(username, password);
    }

    @GetMapping("/method-level/{username}/{password}")
    public String testMethodLevelConfig(@PathVariable String username, @PathVariable String password) {
        return """
                [方法级粒度配置]
                
                同一服务的不同方法可以有不同的配置:
                @Method(name="login", timeout=2000)   // 登录快响应
                @Method(name="query", timeout=10000)   // 查询可慢
                
                优势: 细粒度控制、针对性优化、差异化容错
                
                调用结果:
                """ + dynamicConfigUserService.userLogin(username, password);
    }
}
