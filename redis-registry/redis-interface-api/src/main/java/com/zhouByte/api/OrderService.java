package com.zhouByte.api;

/**
 * 订单服务接口 - Dubbo RPC 服务契约
 * 
 * Dubbo 接口说明:
 *   - 此接口定义了订单相关的远程服务契约
 *   - Provider 实现此接口并通过 @DubboService 暴露
 *   - Consumer 通过 @DubboReference 引用进行调用
 * 
 * 服务方法:
 *   - createOrder: 创建订单
 *     参数:
 *       - userId: 用户ID，String 类型
 *       - productId: 商品ID，String 类型
 *       - quantity: 购买数量，int 类型
 *     返回值: 订单创建结果(如订单ID)，String 类型
 *   - getOrderStatus: 查询订单状态
 *     参数:
 *       - orderId: 订单ID，String 类型
 *     返回值: 订单状态信息，String 类型
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
     * @return 订单状态信息
     */
    String getOrderStatus(String orderId);
}
