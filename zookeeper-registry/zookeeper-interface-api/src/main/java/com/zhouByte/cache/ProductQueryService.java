package com.zhouByte.cache;

/**
 * 商品查询服务接口 - 用于演示结果缓存功能
 * 
 * Dubbo 结果缓存说明:
 *   - 缓存可以避免重复调用，减少网络开销和 Provider 压力
 *   - 通过 @DubboReference(cache = "true") 或 cache = "lru" 启用
 *   - 缓存策略:
 *     1. lru: 最近最少使用策略，默认缓存 1000 条记录
 *     2. threadlocal: 线程级别缓存，仅当前线程可见
 *     3. jcache: 集成 JSR107 JCache 标准，支持自定义缓存实现
 *   - 缓存作用域:
 *     - Consumer 端缓存: 在 Consumer 侧缓存调用结果
 *     - 方法级缓存: 可对单个方法启用缓存
 *   - 缓存失效:
 *     - 写操作(增删改)不应使用缓存
 *     - 读操作(查询)适合使用缓存
 *     - 可通过 cache.size 配置缓存大小
 * 
 * 服务方法:
 *   - getProductInfo: 查询商品信息(适合缓存)
 *     参数:
 *       - productId: 商品ID
 *     返回值: 商品信息
 *   - getProductPrice: 查询商品价格(适合缓存)
 *     参数:
 *       - productId: 商品ID
 *     返回值: 商品价格
 */
public interface ProductQueryService {

    /**
     * 查询商品信息 - 适合缓存的读操作
     * @param productId 商品ID
     * @return 商品信息
     */
    String getProductInfo(String productId);

    /**
     * 查询商品价格 - 适合缓存的读操作
     * @param productId 商品ID
     * @return 商品价格
     */
    String getProductPrice(String productId);
}
