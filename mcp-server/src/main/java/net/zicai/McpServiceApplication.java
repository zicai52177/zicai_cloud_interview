package net.zicai;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wangdi
 * @date 2026/5/4 20:02
 * @description
 */
@Slf4j
@SpringBootApplication
public class McpServiceApplication {
    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(McpServiceApplication.class, args);
    }
}