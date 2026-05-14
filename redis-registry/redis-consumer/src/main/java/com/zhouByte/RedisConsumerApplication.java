package com.zhouByte;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Dubbo 服务消费者启动类 - Redis 注册中心
 * 
 * Dubbo 核心注解说明:
 * @EnableDubbo:
 *   作用: 启用 Dubbo 的自动配置和服务引用功能
 *   功能:
 *     - 扫描 @DubboService 注解的类，将其暴露为 Dubbo RPC 服务
 *     - 扫描 @DubboReference 注解的字段，创建远程服务代理
 *     - 初始化 Dubbo 配置中心、注册中心、协议等组件
 *   等价配置:
 *     - 可在 application.yaml 中配置 dubbo.scan.base-packages 指定扫描路径
 *     - 也可使用 @DubboComponentScan 注解自定义扫描路径
 * 
 * @SpringBootApplication:
 *   作用: Spring Boot 核心注解，标记为 Spring Boot 应用
 *   组成:
 *     - @SpringBootConfiguration: 标记为配置类
 *     - @EnableAutoConfiguration: 启用自动配置
 *     - @ComponentScan: 组件扫描
 */
@SpringBootApplication
@EnableDubbo
public class RedisConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RedisConsumerApplication.class, args);
    }
}
