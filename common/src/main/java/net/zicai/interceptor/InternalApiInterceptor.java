package net.zicai.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.InternalApiConfig;
import net.zicai.enums.BizCodeEnum;
import net.zicai.util.CommonUtil;
import net.zicai.util.JsonData;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 内部 API 拦截器
 * 保护仅供内部 Feign 调用的接口，防止外部直接访问
 */
@Slf4j
@Component
public class InternalApiInterceptor implements HandlerInterceptor {

    private final InternalApiConfig internalApiConfig;

    public InternalApiInterceptor(InternalApiConfig internalApiConfig) {
        this.internalApiConfig = internalApiConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String internalSecret = request.getHeader(InternalApiConfig.HEADER_NAME);
        String configSecret = internalApiConfig.getSecret();

        // 如果未配置内部密钥则跳过验证（兼容本地开发）
        if (configSecret == null || configSecret.isEmpty()) {
            log.warn("内部API密钥未配置，跳过验证，uri:{}", request.getRequestURI());
            return true;
        }

        if (configSecret.equals(internalSecret)) {
            return true;
        }

        log.warn("内部API鉴权失败，uri:{}, remoteAddr:{}", request.getRequestURI(), request.getRemoteAddr());
        CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
        return false;
    }
}
