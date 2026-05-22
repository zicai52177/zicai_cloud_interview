package net.zicai.controller.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author wangdi
 * @date 2026/5/11 15:48
 * @description
 */
@Data
public class PackageOrderCreateReq {


    @NotNull(message = "packageId不能为空")
    private Long PackageId;

    @NotNull(message = "payType不能为空")
    private String payType;

    private BigDecimal discount = BigDecimal.ONE;
}
