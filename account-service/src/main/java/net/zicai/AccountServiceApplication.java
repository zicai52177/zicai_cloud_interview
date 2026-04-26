package net.zicai;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Slf4j
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("net.zicai.mapper")
@EnableCaching
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
public class AccountServiceApplication {
    @SneakyThrows
    public static void main(String[] args) {
        // 加载 .env 文件到系统属性，供 yml 占位符引用
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("src/main/resources")
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
                log.debug("加载环境变量: {}=***", entry.getKey());
            });
            log.info(".env 文件加载成功，共 {} 个配置项", dotenv.entries().size());
        } catch (Exception e) {
            log.warn(".env 文件加载失败，将使用 yml 中的默认值: {}", e.getMessage());
        }

        SpringApplication.run(AccountServiceApplication.class, args);
    }

}