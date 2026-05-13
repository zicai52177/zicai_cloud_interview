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
     * 查询套餐列表
     *
     * @return 套餐列表（含权益信息）
     */
    @Operation(summary = "查询套餐列表", description = "查询所有上线状态的套餐，包含套餐内权益信息")
    @GetMapping("/packages")
    public JsonData listPackages() {
        log.info("查询套餐列表请求");
        List<ProductPackageDTO> packageList = productPackageService.listPackages(StatusEnum.ON.name());
        return JsonData.buildSuccess(packageList);
    }

    /**
     * 查询权益列表
     *
     * @return 权益列表
     */
    @Operation(summary = "查询权益列表", description = "查询所有可用的权益列表，默认返回上线状态的权益")
    @GetMapping("/benefits")
    public JsonData listBenefits() {
        log.info("查询权益列表请求");
        List<BenefitDTO> benefitList = benefitService.list(StatusEnum.ON.name());
        return JsonData.buildSuccess(benefitList);
    }
}
