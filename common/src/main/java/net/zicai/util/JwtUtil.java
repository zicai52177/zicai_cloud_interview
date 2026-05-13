package net.zicai.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.AccountDTO;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类
 * 密钥通过 JwtConfig 从配置文件注入，禁止硬编码
 */
@Slf4j
public class JwtUtil {

    private static final String LOGIN_SUBJECT = "ai";

    /**
     * JWT密钥，由 JwtConfig 在应用启动时通过 initSecretKey() 注入
     * 配置项: jwt.secret-key (来自环境变量 JWT_SECRET_KEY)
     */
    private static volatile SecretKey TENANT_ACCOUNT_KEY;

    // 签名算法
    private final static SecureDigestAlgorithm<SecretKey, SecretKey> ALGORITHM = Jwts.SIG.HS256;
    // token过期时间，30天
    private static final long EXPIRED = 1000L * 60 * 60 * 24 * 30;

    /**
     * 初始化密钥（由 JwtConfig 调用）
     */
    public static void initSecretKey(String secretKey) {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException("JWT密钥长度不能少于32个字符，请检查配置项 jwt.secret-key");
        }
        TENANT_ACCOUNT_KEY = Keys.hmacShaKeyFor(secretKey.getBytes());
        log.info("JWT密钥初始化完成");
    }

    /**
     * 生成JWT
     * @param accountDTO 登录账户信息
     * @return 生成的JWT字符串
     * @throws NullPointerException 如果传入的accountDTO为空
     */
    public static String geneTenantAccountLoginJWT(AccountDTO accountDTO) {
        if (TENANT_ACCOUNT_KEY == null) {
            throw new IllegalStateException("JWT密钥未初始化，请检查 JwtConfig 配置");
        }
        if (accountDTO == null) {
            throw new NullPointerException("对象为空");
        }

        //机构版，创建 JWT token
        String token = Jwts.builder()
                .subject(LOGIN_SUBJECT)
                .claim("accountId", accountDTO.getId())
                .claim("username", accountDTO.getUsername())
                .claim("headImg", accountDTO.getHeadImg())
                //通过这个是否为空判断是否机构版还是商家版
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRED))
                .signWith(TENANT_ACCOUNT_KEY, ALGORITHM)  // 直接使用KEY即可
                .compact();
        // 添加自定义前缀
        return addPrefix(token, LOGIN_SUBJECT);


    }

    /**
     * 校验JWT
     * @param token JWT字符串
     * @return JWT的Claims部分
     * @throws IllegalArgumentException 如果传入的token为空或只包含空白字符
     * @throws RuntimeException 如果JWT签名验证失败、JWT已过期或JWT解密失败
     */
    public static Claims checkTenantAccountLoginJWT(String token) {
        try {
            if (TENANT_ACCOUNT_KEY == null) {
                throw new IllegalStateException("JWT密钥未初始化，请检查 JwtConfig 配置");
            }
            log.debug("开始校验 JWT: {}", token);
            // 校验 Token 是否为空
            if (token == null || token.trim().isEmpty()) {
                log.error("Token 不能为空");
                throw new IllegalArgumentException("Token 不能为空");
            }
            token = token.trim();
            // 移除前缀
            token = removePrefix(token, LOGIN_SUBJECT);
            log.debug("移除前缀后的 Token: {}", token);
            // 解析 JWT
            Claims payload = Jwts.parser()
                    .verifyWith(TENANT_ACCOUNT_KEY)  //设置签名的密钥, 使用相同的 KEY
                    .build()
                    .parseSignedClaims(token).getPayload();

            log.info("JWT 解密成功，Claims: {}", payload);
            return payload;
        } catch (IllegalArgumentException e) {
            log.error("JWT 校验失败: {}", e.getMessage(), e);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("JWT 签名验证失败: {}", e.getMessage(), e);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("JWT 已过期: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("JWT 解密失败: {}", e.getMessage(), e);
        }
        return null;
    }


    /**
     * 给token添加前缀
     * @param token 原始token字符串
     * @return 添加前缀后的token字符串
     */
    private static String addPrefix(String token,String prefix) {
        return  prefix+token;
    }

    /**
     * 移除token的前缀
     * @param token 带前缀的token字符串
     * @return 移除前缀后的token字符串
     */
    private static String removePrefix(String token,String prefix) {
        if (token.startsWith(prefix)) {
            return token.replace(prefix, "").trim();
        }
        return token;
    }


}