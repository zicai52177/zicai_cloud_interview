package net.zicai.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 王镝
 * @date 20260423
 **/

/**
 * 阿里云OSS配置类
 * 用于配置阿里云对象存储服务的相关参数和创建OSS客户端管理器
 */
@Slf4j
@Configuration
@Data
public class OssConfig {

    /**
     * 安全端点地址（HTTPS）
     */
    @Value("${aliyun.oss.secure-endpoint}")
    private String secureEndpoint;

    /**
     * 访问密钥ID
     */
    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    /**
     * 访问密钥Secret
     */
    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    /**
     * 公开端点地址，如果未配置则使用安全端点作为默认值
     */
    @Value("${aliyun.oss.public-endpoint:${aliyun.oss.secure-endpoint}}")
    private String publicEndpoint;

    /**
     * 安全桶名称
     */
    @Value("${aliyun.oss.secure-bucket-name}")
    private String secureBucketName;

    /**
     * 安全域名
     */
    @Value("${aliyun.oss.secure-domain}")
    private String secureDomain;

    /**
     * 公共桶名称
     */
    @Value("${aliyun.oss.public-bucket-name}")
    private String publicBucketName;

    /**
     * 公共域名
     */
    @Value("${aliyun.oss.public-domain}")
    private String publicDomain;

    /**
     * 创建OSS客户端管理器
     */
    @Bean
    public OssClientManager ossClientManager() {
        // 创建安全存储桶客户端
        OSS secureOss = new OSSClientBuilder().build(secureEndpoint, accessKeyId, accessKeySecret);
        OssClientManager.BucketClient secureBucket = new OssClientManager.BucketClient(
                secureOss, secureBucketName, secureDomain, false);

        // 创建公共存储桶客户端
        OSS publicOss = new OSSClientBuilder().build(publicEndpoint, accessKeyId, accessKeySecret);
        OssClientManager.BucketClient publicBucket = new OssClientManager.BucketClient(
                publicOss, publicBucketName, publicDomain, true);

        log.info("OSS客户端管理器初始化完成，secureEndpoint={}, publicEndpoint={}", secureEndpoint, publicEndpoint);
        return new OssClientManager(secureBucket, publicBucket);
    }

}
