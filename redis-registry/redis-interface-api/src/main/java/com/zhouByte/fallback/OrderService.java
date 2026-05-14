package com.zhouByte.fallback;

/**
 * 订单服务接口 - 用于演示服务降级与 Mock 功能
 * 
 * Dubbo 服务降级说明:
 *   - 当 Provider 不可用时，Consumer 自动调用本地 Mock 类进行降级
 *   - Mock 类命名规则: 接口名 + Mock，如 OrderServiceMock
 *   - Mock 类必须实现原接口
 *   - 通过 @DubboReference(mock = "true") 或 mock = "force:return null" 启用
 * 
 * 降级策略:
 *   - mock = "true": Provider 调用失败时自动降级到 Mock 类
 *   - mock = "force:return null": 强制降级，不调用 Provider，直接返回 null
 *   - mock = "fail:return null": 失败时降级，返回 null
 *   - mock = "com.xxx.MyMock": 指定自定义 Mock 类全限定名
 * 
 * 服务方法:
 *   - createOrder: 创建订单
 *     参数:
 *       - userId: 用户ID
 *       - productId: 商品ID
 *       - quantity: 购买数量
 *     返回值: 订单创建结果
 */
public interface OrderService {

    /**
     * 创建订单
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 购买数量
     * @return 订单创建结果
     */
    String createOrder(String userId, String productId, int quantity);

    /**
     * 查询订单状态
     * @param orderId 订单ID
     * @return 订单状态
     */
    String getOrderStatus(String orderId);
}
