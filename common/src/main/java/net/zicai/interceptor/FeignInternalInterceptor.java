package net.zicai.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.InternalApiConfig;
import org.springframework.stereotype.Component;

/**
 * Feign请求拦截器
 * 自动为所有 Feign 调用添加内部服务密钥 header
 */
@Slf4j
@Component
public class FeignInternalInterceptor implements RequestInterceptor {

    private final InternalApiConfig internalApiConfig;

    public FeignInternalInterceptor(InternalApiConfig internalApiConfig) {
        this.internalApiConfig = internalApiConfig;
    }

    @Override
    public void apply(RequestTemplate template) {
        String secret = internalApiConfig.getSecret();
        if (secret != null && !secret.isEmpty()) {
            template.header(InternalApiConfig.HEADER_NAME, secret);
            log.debug("Feign请求添加内部密钥 header, url:{}", template.url());
        }
    }
}
