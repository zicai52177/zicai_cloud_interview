package net.zicai.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 内部API保护过滤器
 * 禁止外部通过网关访问内部 Feign 调用端点
 */
@Slf4j
@Component
public class InternalApiProtectionFilter implements GlobalFilter, Ordered {

    private static final List<String> INTERNAL_PATHS = List.of(
            "/api/v1/account/benefit/checkAndDeduct",
            "/api/v1/account/benefit/task/updateStatus"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        for (String internalPath : INTERNAL_PATHS) {
            if (path.equals(internalPath)) {
                log.warn("外部请求尝试访问内部API, path:{}, ip:{}",
                        path, exchange.getRequest().getRemoteAddress());
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
