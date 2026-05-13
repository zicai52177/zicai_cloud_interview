package net.zicai.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 内部服务调用配置
 * 用于 Feign 调用时的鉴权
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "feign.internal")
public class InternalApiConfig {

    /**
     * 内部服务调用密钥
     * 配置方式: feign.internal.secret=${FEIGN_INTERNAL_SECRET}
     */
    private String secret;

    /**
     * 内部调用请求头名称
     */
    public static final String HEADER_NAME = "X-Internal-Secret";
}
