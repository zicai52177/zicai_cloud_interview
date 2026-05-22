package net.zicai.service;

import net.zicai.dto.BenefitDTO;
import net.zicai.dto.ProductPackageDTO;

import java.util.List;

/**
 * @author wangdi
 * @date 2026/5/7 15:55
 * @description
 */
public interface ProductPackageService {
    List<ProductPackageDTO> listPackages(String status);

    List<BenefitDTO> getPackageBenefits(Long packageId);
}
