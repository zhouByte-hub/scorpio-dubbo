package com.zhouByte.fallback;

import org.apache.dubbo.config.annotation.DubboService;

/**
 * 订单服务实现 - 正常 Provider 端服务
 * 
 * Dubbo 服务暴露说明:
 * @DubboService 配置:
 *   - interfaceClass = OrderService.class: 指定暴露的服务接口
 *   - group = "fallback-demo": 服务分组，标识此服务用于降级演示
 *   - version = "1.0.0": 服务版本号
 *   - weight = 100: 服务权重，数值越大分配的请求越多
 * 
 * 服务降级配合:
 *   - Consumer 端通过 @DubboReference(mock = "true") 启用降级
 *   - 当此服务不可用时，Consumer 自动调用 OrderServiceMock
 *   - Mock 类命名规则: 接口名 + Mock，必须与接口在同一包下
 */
@DubboService(
        interfaceClass = OrderService.class,
        group = "fallback-demo",
        version = "1.0.0"
)
public class OrderServiceImpl implements OrderService {

    @Override
    public String createOrder(String userId, String productId, int quantity) {
        return "[正常调用] 订单创建成功\n"
                + "- userId: " + userId + "\n"
                + "- productId: " + productId + "\n"
                + "- quantity: " + quantity + "\n"
                + "- orderId: ORDER-" + System.currentTimeMillis();
    }

    @Override
    public String getOrderStatus(String orderId) {
        return "[正常调用] 订单状态查询成功\n"
                + "- orderId: " + orderId + "\n"
                + "- status: 已发货\n"
                + "- carrier: SF Express";
    }
}
