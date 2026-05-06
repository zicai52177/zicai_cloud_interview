package net.zicai.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wangdi
 * @date 2026/5/6 17:12
 * @description
 */
@Data
@Builder
@Schema(name = "WechatQrCodeDTO", description = "微信二维码信息")
public class WechatQrCodeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "二维码唯一标识,UUID生成的32位字符串，全局唯一")
    private String sceneId;

    @Schema(description = "二维码图片URL 二维码图片URL，前端可直接使用`<img>`标签显示")
    private String qrCodeUrl;

    @Schema(description = "二维码过期时间（秒）有效期120秒，超时后二维码失效")
    private Integer expireTime;

    //初始状态为`WAITING`，扫码后变为`SCANNED`，登录成功变为`CONFIRMED`
    @Schema(description = "二维码状态：WAITING-等待扫码，SCANNED-已扫码，CONFIRMED-已确认，EXPIRED-已过期")
    private String status;

    @Schema(description = "创建时间,时间戳，用于计算剩余有效时间")
    private Long createTime;
}