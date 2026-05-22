package net.zicai.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author 王镝
 * @date 2026 04 16
 **/

@Data
@Configuration
@ConfigurationProperties(prefix = "sms")
public class SmsConfig {

    private String host = "https://gyytz.market.alicloudapi.com";

    private String path = "/sms/smsSend";

    private String appCode ;

    private String smsSignId ;

    private String templateId ;

}
