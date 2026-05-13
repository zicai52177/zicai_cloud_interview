package net.zicai.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.zicai.util.JwtUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 * 从配置文件读取密钥并注入到 JwtUtil
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT签名密钥（至少32个字符）
     * 配置方式: jwt.secret-key=${JWT_SECRET_KEY}
     */
    private String secretKey;

    @PostConstruct
    public void init() {
        JwtUtil.initSecretKey(secretKey);
        log.info("JwtConfig 初始化完成，密钥已注入 JwtUtil");
    }
}
