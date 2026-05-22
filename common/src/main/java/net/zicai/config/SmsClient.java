package net.zicai.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.SmsDTO;
import net.zicai.util.HttpUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 王镝
 * @date 2026 04 16
 **/

@Component
@Slf4j
public class SmsClient {

    @Resource
    private SmsConfig smsConfig;

    public String sendSms(SmsDTO smsDTO) {
        String url = smsConfig.getHost() + smsConfig.getPath();


        // 1. 构建请求头
        Map<String, String> headers = new HashMap<>();
        // Authorization 格式：APPCODE + 空格 + AppCode
        headers.put("Authorization", "APPCODE " + smsConfig.getAppCode());

        // 2. 构建请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("mobile",smsDTO.getMobile());
        params.put("param", smsDTO.getParam());
        params.put("smsSignId", smsConfig.getSmsSignId());
        params.put("templateId", smsConfig.getTemplateId());

        String resp = HttpUtil.postForm(url, headers, params);
        log.info("[发送短信] req : {} , resp : {}",smsDTO.getMobile(),resp);
        return resp;
    }

}
