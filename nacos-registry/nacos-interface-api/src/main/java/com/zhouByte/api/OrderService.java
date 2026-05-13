package com.zhouByte.api;

/**
 * 订单服务接口
 */
public interface OrderService {

    String createOrder(String userId, String productId, int quantity);

    String getOrderStatus(String orderId);
}
