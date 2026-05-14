package com.zhouByte.cache;

import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品查询服务实现 - 演示结果缓存功能
 * 
 * Dubbo 结果缓存说明:
 * @DubboService 配置:
 *   - interfaceClass = ProductQueryService.class: 指定暴露的服务接口
 *   - group = "cache-demo": 服务分组，标识此服务用于缓存演示
 *   - version = "1.0.0": 服务版本号
 * 
 * 缓存配置:
 *   - Consumer 端通过 @DubboReference(cache = "lru") 启用缓存
 *   - 缓存策略:
 *     1. lru: 最近最少使用，默认缓存 1000 条
 *     2. threadlocal: 线程级别缓存
 *     3. jcache: 集成 JSR107 JCache
 *   - 缓存大小: 可通过 cache.size 参数配置
 * 
 * 模拟数据:
 *   - 使用 Map 模拟数据库查询
 *   - 每次调用会记录日志，便于观察缓存是否生效
 *   - 缓存命中时不会执行此方法，直接返回缓存结果
 */
@DubboService(
        interfaceClass = ProductQueryService.class,
        group = "cache-demo",
        version = "1.0.0"
)
public class ProductQueryServiceImpl implements ProductQueryService {

    private static final Logger logger = LoggerFactory.getLogger(ProductQueryServiceImpl.class);

    private static final Map<String, String> PRODUCTS = new HashMap<>();
    private static final Map<String, String> PRICES = new HashMap<>();

    static {
        PRODUCTS.put("P001", "iPhone 15 Pro Max");
        PRODUCTS.put("P002", "MacBook Pro 14");
        PRODUCTS.put("P003", "AirPods Pro 2");
        PRODUCTS.put("P004", "iPad Air");

        PRICES.put("P001", "¥9999");
        PRICES.put("P002", "¥16999");
        PRICES.put("P003", "¥1899");
        PRICES.put("P004", "¥4799");
    }

    @Override
    public String getProductInfo(String productId) {
        logger.info("Provider 执行查询: getProductInfo({})", productId);
        String productName = PRODUCTS.getOrDefault(productId, "商品不存在");
        return "[Provider 查询] 商品信息\n"
                + "- productId: " + productId + "\n"
                + "- productName: " + productName + "\n"
                + "- timestamp: " + System.currentTimeMillis();
    }

    @Override
    public String getProductPrice(String productId) {
        logger.info("Provider 执行查询: getProductPrice({})", productId);
        String price = PRICES.getOrDefault(productId, "价格未知");
        return "[Provider 查询] 商品价格\n"
                + "- productId: " + productId + "\n"
                + "- price: " + price + "\n"
                + "- timestamp: " + System.currentTimeMillis();
    }
}
