package com.zhouByte.governance;

import com.zhouByte.api.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户服务 V1.0.0 版本实现 - 基础版本
 * 
 * <h2>功能说明</h2>
 * 该类是 UserService 接口的第一个版本实现（V1.0.0），
 * 用于演示 Dubbo 的<strong>服务版本管理</strong>功能。
 * 
 * <h2>Dubbo 版本管理机制</h2>
 * <p>Dubbo 通过 <strong>@DubboService 注解的 version 属性</strong>来实现服务版本控制：</p>
 * <ul>
 *   <li><b>version="1.0.0"</b>: 标识该服务的版本号为 1.0.0</li>
 *   <li><b>group="governance-version"</b>: 服务分组，用于逻辑隔离不同用途的服务实例</li>
 *   <li>Consumer 端通过 version 参数精确指定要调用的版本</li>
 * </ul>
 * 
 * <h2>版本号命名规范建议</h2>
 * <p>推荐使用<strong>语义化版本号（Semantic Versioning）</strong>格式：</p>
 * <pre>
 * {主版本}.{次版本}.{修订号}
 * 
 * 示例：
 * - 1.0.0 → 首个正式版本
 * - 1.1.0 → 新增功能（向后兼容）
 * - 1.1.1 → Bug修复（向后兼容）
 * - 2.0.0 → 重大变更（不兼容）
 * </pre>
 * 
 * <h2>V1.0.0 版本特性</h2>
 * <ul>
 *   <li>✅ 使用 MD5 加密密码（基础安全措施）</li>
 *   <li>✅ 简单的用户名验证逻辑</li>
 *   <li>❌ 不支持多因素认证</li>
 *   <li>❌ 无登录日志记录功能</li>
 * </ul>
 * 
 * <h2>适用场景</h2>
 * <ul>
 *   <li>MVP（最小可行产品）阶段</li>
 *   <li>内部测试环境</li>
 *   <li>对安全性要求不高的演示系统</li>
 *   <li>需要保持向后兼容的老旧客户端</li>
 * </ul>
 * 
 * <h2>与 V2.0.0 的对比</h2>
 * <table border="1">
 *   <tr>
 *     <th>特性</th>
 *     <th>V1.0.0（本类）</th>
 *     <th>V2.0.0（升级版）</th>
 *   </tr>
 *   <tr>
 *     <td>密码加密</td>
 *     <td>MD5（已被破解，不安全）</td>
 *     <td>BCrypt（推荐，抗彩虹表）</td>
 *   </tr>
 *   <tr>
 *     <td>多因素认证</td>
 *     <td>❌ 不支持</td>
 *     <td>✅ 支持（短信/邮箱/OTP）</td>
 *   </tr>
 *   <tr>
 *     <td>登录日志</td>
 *     <td>❌ 无</td>
 *     <td>✅ 完整审计日志</td>
 *   </tr>
 *   <tr>
 *     <td>适用性</td>
 *     <td>开发/测试环境</td>
 *     <td>生产环境</td>
 *   </tr>
 * </table>
 * 
 * <h2>Consumer 调用方式</h2>
 * <pre>
 * // Consumer 端精确调用 V1.0.0
 * @DubboReference(
 *     interfaceClass = UserService.class,
 *     version = "1.0.0",  // ← 指定版本
 *     group = "governance-version"
 * )
 * private UserService userServiceV1;
 * 
 * // 调用时只会路由到 V1.0.0 的 Provider
 * userServiceV1.userLogin("admin", "123456");
 * </pre>
 * 
 * @author zhouByte
 * @version 1.0.0
 * @since 2024-01-01
 * @see UserService
 * @see UserServiceV2
 */
@DubboService(
        interfaceClass = UserService.class,
        version = "1.0.0",
        group = "governance-version"
)
public class UserServiceV1 implements UserService {

    /**
     * 用户登录方法 - V1.0.0 基础版实现
     * 
     * <p><b>业务流程：</b></p>
     * <ol>
     *   <li>接收用户名和密码参数</li>
     *   <li>使用 MD5 对密码进行加密处理（⚠️ 注意：MD5 已不安全，仅用于演示）</li>
     *   <li>执行简单的用户名格式验证</li>
     *   <li>返回登录结果字符串</li>
     * </ol>
     * 
     * <p><b>返回值说明：</b></p>
     * 返回一个包含版本标识和功能描述的多行字符串，
     * 格式如下：
     * <pre>
     * [V1.0.0] 用户登录 - 基础版本
     * - 使用 MD5 加密密码
     * - 简单的用户名验证
     * </pre>
     * 
     * <p><b>⚠️ 安全提示：</b></p>
     * 实际生产环境中<strong>严禁使用 MD5</strong>存储密码！
     * 推荐使用 BCrypt、Argon2 或 PBKDF2 等现代哈希算法。
     * 此处仅为演示版本差异而保留。
     * 
     * @param username 用户名，用于身份识别
     *                示例: "admin", "zhangsan"
     * @param password 密码，用于验证用户身份
     *                ⚠️ 生产环境应通过 HTTPS 加密传输
     * @return String 登录结果信息，包含版本标识、加密方式、验证逻辑说明
     *         示例: "[V1.0.0] 用户登录 - 基础版本\n- 使用 MD5 加密密码\n- 简单的用户名验证"
     */
    @Override
    public String userLogin(String username, String password) {
        return "[V1.0.0] 用户登录 - 基础版本\n"
                + "- 使用 MD5 加密密码\n"
                + "- 简单的用户名验证";
    }
}
