package net.zicai.config;

/**
 * @author 王镝
 * @date 2026 04 16
 **/


public class RedisKeyManager {
    /**
     * 验证码key  第一个是唯一标识  第二个是类型
     */
    public static final String CHECK_CODE_KEY = "znoffer:code:%s:%s";


    /**
     * 微信access_token缓存,加入appID隔离
     * "wechat:access_token"
     */
    public static final String WECHAT_ACCESS_TOKEN = "zcoffer:wechat:access_token:%s";


    /**
     * 分布式锁缓存    key为sceneid
     */
    public static final String REDIS_WECHAT_ACCESS_TOKEN_LOCK = "zcoffer:wechat:access_token:lock:%s";

    /**
     * 微信二维码缓存   key为公众号appid
     */
    public static final String WECHAT_QR_CODE_SCENE = "zcoffer:wechat:qrcode:scene:%s";

    /**
     * 存储用户登陆结果  key为sceneid
     */
    public static final String WECHAT_USER_LOGIN_RESULT = "zcoffer:wechat:login:result:%s";

}
