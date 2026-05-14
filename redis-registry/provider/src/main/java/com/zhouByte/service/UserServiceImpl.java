package com.zhouByte.service;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户服务基础实现
 * 
 * Dubbo 核心注解说明:
 * @DubboService - 将当前类声明为 Dubbo RPC 服务提供者
 *   作用: 将 Spring Bean 暴露为 Dubbo 远程服务，自动注册到注册中心(Nacos)
 *   核心参数:
 *     - interfaceClass: 指定服务接口类型，默认为实现的接口
 *     - version: 服务版本号，用于多版本管理和灰度发布
 *     - group: 服务分组，用于服务隔离和逻辑分组
 *     - timeout: 方法调用超时时间(毫秒)，默认 1000ms
 *     - retries: 失败重试次数，默认 2 次(不含首次调用)
 *     - loadbalance: 负载均衡策略，可选值: random/roundrobin/leastactive/consistenthash
 *     - cluster: 集群容错模式，可选值: failover/failfast/failsafe/failback/forking
 *     - weight: 服务权重，数值越大分配的请求越多
 *     - filter: 指定服务过滤器，用于拦截请求进行处理
 *     - methods: 方法级配置数组，可对单个方法设置独立参数
 */
@DubboService
public class UserServiceImpl implements UserService {

    @Override
    public String userLogin(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        return username + "登录成功";
    }

}
