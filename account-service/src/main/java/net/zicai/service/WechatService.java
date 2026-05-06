package net.zicai.service;

import net.zicai.util.JsonData;

/**
 * @author wangdi
 * @date 2026/5/5 20:56
 * @description
 */
public interface WechatService {
    String getAccessToken();

    JsonData generateQrcode();

    JsonData getLoginResult(String sceneId);

    JsonData handleScanCallback(String sceneId, String openid);
}
