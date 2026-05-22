package net.zicai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangdi
 * @date 2026/5/1 21:34
 * @description
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "searxng")
public class SearxngProperties {

    /** SearXNG 服务地址 */
    private String baseUrl = "http://localhost:8081";

    /** 返回格式（默认 json） */
    private String format = "json";

    /** 搜索语言（默认中文） */
    private String language = "zh";

    /** 搜索引擎（多个用逗号分隔） */
    private String engines = "bing";
}
