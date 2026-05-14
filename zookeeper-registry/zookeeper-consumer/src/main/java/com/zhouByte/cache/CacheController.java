package com.zhouByte.cache;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 结果缓存测试控制器
 * 演示 Dubbo 结果缓存功能，减少重复调用的网络开销
 * 
 * Dubbo 结果缓存说明:
 * @DubboReference 配置:
 *   - cache: 启用结果缓存
 *     1. cache = "true" 或 cache = "lru": LRU 缓存策略
 *        - 最近最少使用策略
 *        - 默认缓存 1000 条记录
 *        - 缓存满时淘汰最久未使用的记录
 *     2. cache = "threadlocal": 线程级别缓存
 *        - 仅当前线程可见
 *        - 适用于同一线程内多次调用相同方法
 *     3. cache = "jcache": JSR107 JCache 集成
 *        - 支持自定义缓存实现
 *        - 可配置缓存过期时间、大小等
 * 
 * 缓存适用场景:
 *   - 读操作(查询): 适合缓存，减少重复调用
 *   - 写操作(增删改): 不适合缓存，应每次调用
 *   - 数据变化频繁: 不适合缓存，可能返回过期数据
 *   - 数据相对静态: 适合缓存，如商品信息、配置数据
 * 
 * 缓存配置参数:
 *   - cache.size: 缓存大小，默认 1000
 *   - 可通过 @Method 注解对单个方法配置缓存
 * 
 * 缓存失效策略:
 *   - LRU: 淘汰最久未使用的记录
 *   - 手动清除: 通过更新操作触发缓存失效
 *   - 过期时间: JCache 支持配置 TTL
 */
@RestController
@RequestMapping("/cache")
public class CacheController {

    /**
     * 引用商品查询服务 - 启用 LRU 缓存
     * @DubboReference 配置:
     *   - interfaceClass = ProductQueryService.class: 指定服务接口
     *   - group = "cache-demo": 匹配服务分组
     *   - version = "1.0.0": 匹配服务版本号
     *   - cache = "lru": 启用 LRU 缓存策略
     */
    @DubboReference(
            interfaceClass = ProductQueryService.class,
            group = "cache-demo",
            version = "1.0.0",
            cache = "lru"
    )
    private ProductQueryService cachedProductService;

    /**
     * 引用商品查询服务 - 不启用缓存(用于对比)
     */
    @DubboReference(
            interfaceClass = ProductQueryService.class,
            group = "cache-demo",
            version = "1.0.0"
    )
    private ProductQueryService noCacheProductService;

    /**
     * 测试缓存命中 - 多次调用相同参数，观察缓存效果
     * 第一次调用: 访问 Provider，记录 timestamp
     * 后续调用: 命中缓存，返回相同 timestamp
     */
    @GetMapping("/hit/{productId}")
    public String testCacheHit(@PathVariable String productId) {
        StringBuilder result = new StringBuilder();
        result.append("[结果缓存 - 缓存命中测试]\n");
        result.append("多次调用相同参数，观察 timestamp 是否相同\n\n");

        for (int i = 1; i <= 3; i++) {
            result.append("第 ").append(i).append(" 次调用:\n");
            result.append(cachedProductService.getProductInfo(productId)).append("\n\n");
        }

        result.append("说明: 如果 timestamp 相同，说明缓存命中成功");
        return result.toString();
    }

    /**
     * 测试无缓存调用 - 每次调用都会访问 Provider
     */
    @GetMapping("/no-cache/{productId}")
    public String testNoCache(@PathVariable String productId) {
        StringBuilder result = new StringBuilder();
        result.append("[结果缓存 - 无缓存对比测试]\n");
        result.append("每次调用都会访问 Provider，timestamp 应该不同\n\n");

        for (int i = 1; i <= 3; i++) {
            result.append("第 ").append(i).append(" 次调用:\n");
            result.append(noCacheProductService.getProductInfo(productId)).append("\n\n");
        }

        result.append("说明: 如果 timestamp 不同，说明每次都访问了 Provider");
        return result.toString();
    }

    /**
     * 测试不同参数缓存 - 不同参数独立缓存
     */
    @GetMapping("/multi-product")
    public String testMultiProductCache() {
        StringBuilder result = new StringBuilder();
        result.append("[结果缓存 - 多参数缓存测试]\n");
        result.append("不同参数独立缓存，互不影响\n\n");

        String[] productIds = {"P001", "P002", "P003"};

        result.append("第一次查询所有商品:\n");
        for (String productId : productIds) {
            result.append(cachedProductService.getProductInfo(productId)).append("\n\n");
        }

        result.append("第二次查询所有商品(应命中缓存):\n");
        for (String productId : productIds) {
            result.append(cachedProductService.getProductInfo(productId)).append("\n\n");
        }

        return result.toString();
    }

    /**
     * 测试商品价格缓存
     */
    @GetMapping("/price/{productId}")
    public String testPriceCache(@PathVariable String productId) {
        StringBuilder result = new StringBuilder();
        result.append("[结果缓存 - 价格缓存测试]\n\n");

        result.append("第 1 次查询价格:\n");
        result.append(cachedProductService.getProductPrice(productId)).append("\n\n");

        result.append("第 2 次查询价格(应命中缓存):\n");
        result.append(cachedProductService.getProductPrice(productId)).append("\n\n");

        result.append("第 3 次查询价格(应命中缓存):\n");
        result.append(cachedProductService.getProductPrice(productId)).append("\n");

        return result.toString();
    }

    /**
     * 结果缓存说明文档
     */
    @GetMapping("/doc")
    public String getDoc() {
        return """
                [Dubbo 结果缓存说明]
                
                缓存策略:
                  - lru: 最近最少使用，默认 1000 条
                  - threadlocal: 线程级别缓存
                  - jcache: JSR107 JCache 集成
                
                适用场景:
                  - 读操作(查询): 适合缓存
                  - 写操作(增删改): 不适合缓存
                  - 数据静态: 适合缓存
                  - 数据频繁变化: 不适合缓存
                
                配置参数:
                  - cache = "lru": 启用 LRU 缓存
                  - cache.size: 缓存大小
                
                测试接口:
                  - GET /cache/hit/{productId}           - 缓存命中测试
                  - GET /cache/no-cache/{productId}      - 无缓存对比
                  - GET /cache/multi-product             - 多参数缓存
                  - GET /cache/price/{productId}         - 价格缓存
                """;
    }
}
