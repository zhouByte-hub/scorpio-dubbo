package com.zhouByte.balance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 一致性哈希（Consistent Hashing）负载均衡策略实现类
 * 
 * <h2>功能说明</h2>
 * 该类实现了 UserService 接口，使用 Dubbo 的一致性哈希（ConsistentHash）负载均衡策略。
 * 一致性哈希会将相同参数的请求始终路由到同一个 Provider 节点。
 * 
 * <h2>核心原理</h2>
 * <p><b>什么是一致性哈希？</b><br>
 * 一致性哈希是一种特殊的哈希算法，在分布式系统中广泛应用。
 * 它的核心思想是：<strong>将数据和节点映射到同一个哈希环上</strong>。</p>
 * 
 * <pre>
 *                    哈希环示意图（简化版）
 * 
 *                      0
 *                     /\
 *                    /  \
 *              255 /    \ 64  ← Node-C
 *                 /      \
 *                /   192   \
 *               /    /\     \
 *          128 /    /  \     \  ← Node-B
 *             \   /    \    /
 *              \ /  96  \  /
 *               X       \/  ← Node-A
 *              32
 * 
 * 数据分布：
 * - 参数 hash ∈ [32, 96)  → Node-A
 * - 参数 hash ∈ [96, 192) → Node-B  
 * - 参数 hash ∈ [192, 32)  → Node-C（跨越0点）
 * </pre>
 * 
 * <h2>Dubbo 配置说明</h2>
 * <ul>
 *   <li><b>group</b>: "balance-consistenthash" - 标识该服务属于一致性哈希策略组</li>
 *   <li><b>weight</b>: 90 - 权重较高，在虚拟节点数量上有优势</li>
 * </ul>
 * 
 * <h2>关键特性</h2>
 * <ol>
 *   <li><b>相同参数 → 相同节点</b>：保证请求的一致性</li>
 *   <li><b>最小化迁移</b>：新增/删除节点时，只影响相邻的数据</li>
 *   <li><b>平衡性</b>：通过虚拟节点使数据分布更均匀</li>
 * </ol>
 * 
 * <h2>适用场景</h2>
 * <ul>
 *   <li>✅ <strong>缓存系统</strong> - 相同用户的请求命中同一缓存节点</li>
 *   <li>✅ <strong>Session 保持</strong> - 有状态的会话绑定到固定节点</li>
 *   <li>✅ <strong>分库分表</strong> - 按用户ID路由到特定数据库</li>
 *   <li>✅ <strong>消息队列</strong> - 相同 Key 的消息发送到同一消费者</li>
 *   <li>❌ 不适合需要完全均匀分布的场景</li>
 * </ul>
 * 
 * <h2>实际案例</h2>
 * <pre>
 * 场景：电商系统的购物车服务
 * 
 * 用户A（ID=10086）→ 始终路由到 Node-1（购物车数据在该节点）
 * 用户B（ID=10087）→ 始终路由到 Node-2
 * 
 * 优点：
 * - 无需跨节点同步 Session
 * - 缓存命中率极高（本地缓存即可）
 * - 减少网络开销
 * </pre>
 * 
 * <h2>与普通哈希的区别</h2>
 * <table border="1">
 *   <tr>
 *     <th>特性</th>
 *     <th>普通 Hash (mod N)</th>
 *     <th>一致性哈希 (Consistent)</th>
 *   </tr>
 *   <tr>
 *     <td>节点变化时的迁移率</td>
 *     <td>~100%（几乎全部重新分布）</td>
 *     <td>~1/N（仅影响相邻节点）</td>
 *   </tr>
 *   <tr>
 *     <td>数据倾斜问题</td>
 *     <td>严重</td>
 *     <td>轻微（可通过虚拟节点解决）</td>
 *   </tr>
 *   <tr>
 *     <td>扩展性</td>
 *     <td>差</td>
 *     <td>优秀</td>
 *   </tr>
 * </table>
 * 
 * @author zhouByte
 * @version 1.0.0
 * @see UserService
 * @see org.apache.dubbo.rpc.cluster.loadbalance.ConsistentHashLoadBalance
 */
@DubboService(
        interfaceClass = UserService.class,
        group = "balance-consistenthash",
        weight = 90
)
public class UserServiceConsistentHashImpl implements UserService {

    /**
     * 用户登录方法 - 使用一致性哈希负载均衡策略
     * 
     * <p><b>执行流程：</b></p>
     * <ol>
     *   <li>接收用户名和密码参数</li>
     *   <li>模拟55ms的业务处理</li>
     *   <li>计算 username 的 hashCode 并对100取模（模拟哈希环位置）</li>
     *   <li>返回包含 hash 值的结果，便于观察相同参数是否路由到同一节点</li>
     * </ol>
     * 
     * <p><b>如何验证一致性？</b><br>
     * 多次调用相同参数（如 username="admin"），观察返回的 hash 值是否一致：<br>
     * 如果一致 → 说明被路由到了同一个 Provider ✅<br>
     * 如果不一致 → 可能是配置错误或使用了其他策略 ❌
     * 
     * @param username 用户名（作为哈希键值，决定路由目标）
     *                相同的 username 会产生相同的 hash，从而路由到同一节点
     * @param password 密码
     * @return String 包含策略标识、用户名、hash值、权重的结果
     *         示例: "[CONSISTENTHASH] admin 登录成功 (hash=42, 权重=90)"
     *         其中 hash=42 表示该用户被映射到哈希环的第42个位置
     */
    @Override
    public String userLogin(String username, String password) {
        simulateProcessing(55);
        
        /**
         * 计算用户名的哈希值
         * 
         * 说明：
         * 1. username.hashCode() - Java 默认的 hashCode 算法
         * 2. Math.abs() - 取绝对值，避免负数（虽然取模后负数也能正确处理，但更直观）
         * 3. % 100 - 对100取模，将哈希空间限制在 [0, 99] 范围内
         * 
         * 注意：这只是演示用途，实际 Dubbo 内部使用的是更复杂的一致性哈希算法，
         * 包括虚拟节点（Virtual Nodes）来优化数据分布均匀性。
         */
        int hash = Math.abs(username.hashCode() % 100);
        
        return "[CONSISTENTHASH] " + username + " 登录成功 (hash=" + hash + ", 权重=90)";
    }

    /**
     * 模拟业务处理耗时
     * 
     * @param ms 延迟毫秒数
     */
    private void simulateProcessing(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
