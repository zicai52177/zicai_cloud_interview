package net.zicai.config;

import lombok.extern.slf4j.Slf4j;
import net.zicai.interceptor.AccountLoginInterceptor;
import net.zicai.interceptor.InternalApiInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author 王镝
 * @date 20260422
 **/

@Slf4j
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final InternalApiInterceptor internalApiInterceptor;

    public InterceptorConfig(InternalApiInterceptor internalApiInterceptor) {
        this.internalApiInterceptor = internalApiInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 内部API拦截器（优先级最高）：验证内部服务调用密钥
        registry.addInterceptor(internalApiInterceptor)
                .addPathPatterns(
                        "/api/v1/account/benefit/checkAndDeduct",
                        "/api/v1/account/benefit/task/updateStatus"
                )
                .order(0);

        // 登录拦截器
        registry.addInterceptor(new AccountLoginInterceptor())
                .addPathPatterns("/**")
                //不拦截的路径
                .excludePathPatterns(
                        "/api/v1/account/login",
                        "/api/v1/account/captcha",
                        "/api/v1/account/send_check_code",
                        "/api/v1/banner/*",
                        // 微信登录相关接口
                        "/api/v1/wechat/qrcode",
                        "/api/v1/wechat/qrcode/status",
                        "/api/v1/wechat/login/result",
                        "/api/v1/wechat/auth/url",
                        "/api/v1/wechat/auth/callback",
                        // 微信回调接口
                        "/api/v1/wechat/callback/**",
                        "/api/v1/wechat/scan/callback",
                        // 产品相关接口（不需要登录）
                        "/api/v1/product/packages",
                        "/api/v1/product/benefits",
                        "/api/v1/product/package/detail",
                        "/api/v1/product/benefit/detail",
                        // 内部请求（由 InternalApiInterceptor 保护，不需要登录拦截）
                        "/api/v1/account/benefit/checkAndDeduct",
                        "/api/v1/account/benefit/task/updateStatus",
                        // 支付回调接口（不需要登录）
                        "/api/v1/order/callback/wechat_pay",
                        "/api/v1/order/callback/alipay",
                        // 静态资源
                        "/favicon.ico",
                        "/api/v1/pay/test/native",
                        "/error")
                .order(1);
    }
}
