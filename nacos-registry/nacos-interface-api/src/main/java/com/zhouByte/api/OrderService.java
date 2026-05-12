package com.zhouByte.api;

public interface OrderService {
    String createOrder(String userId, String productId, int quantity);
    String getOrderStatus(String orderId);
}
