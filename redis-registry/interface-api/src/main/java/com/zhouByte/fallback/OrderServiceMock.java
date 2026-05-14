package com.zhouByte.fallback;

/**
 * 订单服务 Mock 类 - 用于服务降级
 * 
 * Dubbo Mock 降级说明:
 *   - Mock 类命名规则: 接口名 + Mock (如 OrderServiceMock)
 *   - 必须实现原接口 OrderService
 *   - 当 Provider 不可用时，Dubbo 自动调用此 Mock 类
 *   - Mock 类会被 Dubbo 自动发现并注册，无需额外配置
 * 
 * 降级场景:
 *   - Provider 服务宕机
 *   - Provider 网络不可达
 *   - Provider 调用超时
 *   - Provider 返回异常
 * 
 * 使用方式:
 *   - @DubboReference(mock = "true"): 失败时自动降级
 *   - @DubboReference(mock = "force:return null"): 强制降级
 */
public class OrderServiceMock implements OrderService {

    /**
     * Mock 降级方法 - 创建订单
     * 当 Provider 不可用时，返回降级信息
     */
    @Override
    public String createOrder(String userId, String productId, int quantity) {
        return "[服务降级-Mock] 订单服务不可用，已降级到本地处理\n"
                + "- userId: " + userId + "\n"
                + "- productId: " + productId + "\n"
                + "- quantity: " + quantity + "\n"
                + "- 提示: 订单已记录到本地队列，待服务恢复后同步";
    }

    /**
     * Mock 降级方法 - 查询订单状态
     * 当 Provider 不可用时，返回降级信息
     */
    @Override
    public String getOrderStatus(String orderId) {
        return "[服务降级-Mock] 订单服务不可用，已降级到本地处理\n"
                + "- orderId: " + orderId + "\n"
                + "- 状态: 未知(服务不可用)\n"
                + "- 提示: 请稍后重试或联系管理员";
    }
}
