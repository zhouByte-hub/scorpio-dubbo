package com.zhouByte.fallback;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务降级与 Mock 测试控制器
 * 演示 Provider 不可用时自动降级到本地 Mock 类
 * 
 * Dubbo 服务降级说明:
 * @DubboReference 配置:
 *   - mock: 服务降级策略
 *     1. mock = "true": Provider 调用失败时自动降级到 Mock 类
 *        - Mock 类命名规则: 接口名 + Mock (如 OrderServiceMock)
 *        - Mock 类必须实现原接口，与接口在同一包下
 *        - 降级触发条件: 网络异常、超时、Provider 返回异常
 *     2. mock = "force:return null": 强制降级
 *        - 不调用 Provider，直接返回 null
 *        - 用于服务维护期间强制走降级逻辑
 *     3. mock = "fail:return null": 失败时降级
 *        - 与 mock="true" 类似，但明确指定失败时行为
 *     4. mock = "com.xxx.MyMock": 指定自定义 Mock 类
 *        - 使用全限定名指定任意 Mock 实现类
 * 
 * 降级场景:
 *   - Provider 服务宕机或网络不可达
 *   - Provider 调用超时
 *   - Provider 返回业务异常
 *   - 服务维护期间强制降级
 * 
 * 最佳实践:
 *   - 核心服务: 不建议降级，应保证高可用
 *   - 非核心服务: 建议配置降级，提升系统韧性
 *   - Mock 类应返回友好提示，避免用户困惑
 *   - 降级后应记录日志，便于监控和告警
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * 引用订单服务 - 启用失败自动降级
     * @DubboReference 配置:
     *   - interfaceClass = OrderService.class: 指定服务接口
     *   - group = "fallback-demo": 匹配服务分组
     *   - version = "1.0.0": 匹配服务版本号
     *   - mock = "true": 失败时自动降级到 OrderServiceMock
     *   - timeout = 3000: 超时时间 3 秒，超时后触发降级
     */
    @DubboReference(
            interfaceClass = OrderService.class,
            group = "fallback-demo",
            version = "1.0.0",
            mock = "true",
            timeout = 3000
    )
    private OrderService orderService;

    /**
     * 引用订单服务 - 强制降级模式
     * @DubboReference 配置:
     *   - mock = "force:return null": 强制降级，不调用 Provider
     */
    @DubboReference(
            interfaceClass = OrderService.class,
            group = "fallback-demo",
            version = "1.0.0",
            mock = "force:return null"
    )
    private OrderService forceFallbackOrderService;

    /**
     * 测试正常调用 - Provider 可用时返回正常结果
     */
    @GetMapping("/normal/{userId}/{productId}/{quantity}")
    public String testNormalCall(
            @PathVariable String userId,
            @PathVariable String productId,
            @PathVariable int quantity) {
        return """
                [服务降级 - 正常调用]
                当前 Provider 可用，调用正常服务
                
                """ + orderService.createOrder(userId, productId, quantity);
    }

    /**
     * 测试查询订单 - 正常调用
     */
    @GetMapping("/status/{orderId}")
    public String testOrderStatus(@PathVariable String orderId) {
        return """
                [服务降级 - 查询订单]
                
                """ + orderService.getOrderStatus(orderId);
    }

    /**
     * 测试强制降级 - 不调用 Provider，直接返回 Mock 结果
     */
    @GetMapping("/force/{userId}/{productId}/{quantity}")
    public String testForceFallback(
            @PathVariable String userId,
            @PathVariable String productId,
            @PathVariable int quantity) {
        return """
                [服务降级 - 强制降级模式]
                mock = "force:return null"
                不调用 Provider，直接返回 Mock 结果
                
                """ + forceFallbackOrderService.createOrder(userId, productId, quantity);
    }

    /**
     * 服务降级说明文档
     */
    @GetMapping("/doc")
    public String getDoc() {
        return """
                [Dubbo 服务降级与 Mock 说明]
                
                降级策略:
                  1. mock = "true"          - 失败时自动降级
                  2. mock = "force:return null" - 强制降级
                  3. mock = "fail:return null"  - 失败时降级
                  4. mock = "com.xxx.MyMock"    - 自定义 Mock 类
                
                Mock 类规则:
                  - 命名: 接口名 + Mock (如 OrderServiceMock)
                  - 位置: 与接口在同一包下
                  - 实现: 必须实现原接口
                
                测试接口:
                  - GET /fallback/normal/{userId}/{productId}/{quantity} - 正常调用
                  - GET /fallback/status/{orderId} - 查询订单
                  - GET /fallback/force/{userId}/{productId}/{quantity} - 强制降级
                """;
    }
}
