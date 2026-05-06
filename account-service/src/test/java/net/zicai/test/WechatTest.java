package net.zicai.test;

import lombok.extern.slf4j.Slf4j;
import net.zicai.AccountServiceApplication;
import net.zicai.service.WechatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author wangdi
 * @date 2026/5/6 15:51
 * @description
 */

@Slf4j
@SpringBootTest(classes = AccountServiceApplication.class)
@ActiveProfiles("test")
public class WechatTest {

    @Autowired
    private WechatService wechatService;

    @Test
    public void testGetAccessToken() {
        String accessToken = wechatService.getAccessToken();
        log.info("access_token:{}", accessToken);
    }
}
