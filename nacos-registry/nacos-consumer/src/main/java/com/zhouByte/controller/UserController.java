package com.zhouByte.controller;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器 - 通过 Dubbo RPC 调用远程用户服务
 * 
 * Dubbo 服务引用说明:
 * @DubboReference 注解:
 *   作用: 声明对远程 Dubbo 服务的引用，自动创建代理对象
 *   核心参数:
 *     - interfaceClass: 指定要引用的服务接口类型
 *     - loadbalance: 负载均衡策略
 *       - random: 加权随机(默认)
 *       - roundrobin: 加权轮询
 *       - leastactive: 最少活跃优先
 *       - consistenthash: 一致性哈希
 *     - cluster: 集群容错模式
 *       - failover: 失败自动切换重试(默认)
 *       - failfast: 快速失败，只调用一次
 *       - failsafe: 失败安全，异常被忽略
 *       - failback: 失败自动恢复，后台重试
 *       - forking: 并行调用多个，任一成功即返回
 *     - retries: 失败重试次数(不含首次调用)，默认 2 次
 *     - timeout: 调用超时时间(毫秒)，默认 1000ms
 *     - check: 启动时是否检查服务提供者存在，默认 true
 *     - version: 服务版本号，必须与 Provider 匹配
 *     - group: 服务分组，必须与 Provider 匹配
 *     - filter: Consumer 端过滤器
 *     - parameters: 自定义参数键值对
 */
@RestController
public class UserController {

    /**
     * 引用远程 UserService 服务
     * 配置: 随机负载均衡 + 失败自动切换 + 最多重试 2 次
     */
    @DubboReference(loadbalance = "random", cluster = "failover", retries = 2)
    private UserService userService;

    @GetMapping(value = "/login/{username}/{password}")
    public String userLogin(@PathVariable("username") String username, @PathVariable("password") String password){
        return userService.userLogin(username, password);
    }
}
