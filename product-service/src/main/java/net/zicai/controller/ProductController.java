package net.zicai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.BenefitDTO;
import net.zicai.dto.ProductPackageDTO;
import net.zicai.enums.StatusEnum;
import net.zicai.service.BenefitService;
import net.zicai.service.ProductPackageService;
import net.zicai.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wangdi
 * @date 2026/5/7 15:53
 * @description
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/product")
public class ProductController {

    @Autowired
    private BenefitService benefitService;

    @Autowired
    private ProductPackageService productPackageService;

    /**
     * 查询权益列表
     *
     * @param status 状态（可选，ON上线/OFF下线，默认查询ON）
     * @return 权益列表
     */
    @Operation(summary = "查询权益列表", description = "查询所有可用的权益列表，默认返回上线状态的权益")
    @GetMapping("/benefits")
    public JsonData listBenefits(
            @Parameter(description = "权益状态（ON上线/OFF下线，默认ON）")
            @RequestParam(value = "status", required = false) String status) {
        log.info("查询权益列表请求, status:{}", status);
        List<BenefitDTO> benefitList = benefitService.list(status);
        return JsonData.buildSuccess(benefitList);
    }

    /**
     * 查询产品包列表
     *
     * @return 产品包列表
     */
    @Operation(summary = "查询产品包列表", description = "查询所有可用的产品包列表，默认返回上线状态的产品包")
    @GetMapping("/packages")
    public JsonData listPackages(){

        log.info("查询产品包列表请求");
        List<ProductPackageDTO> packageList = productPackageService.listPackages(StatusEnum.ON.name());
        return JsonData.buildSuccess(packageList);
    }
}
