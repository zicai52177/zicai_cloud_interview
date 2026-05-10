package net.zicai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.BenefitDTO;
import net.zicai.dto.ProductPackageDTO;
import net.zicai.mapper.BenefitMapper;
import net.zicai.mapper.PackageBenefitMapper;
import net.zicai.mapper.ProductPackageMapper;
import net.zicai.model.BenefitDO;
import net.zicai.model.PackageBenefitDO;
import net.zicai.model.ProductPackageDO;
import net.zicai.service.ProductPackageService;
import net.zicai.util.SpringBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangdi
 * @date 2026/5/7 15:55
 * @description
 */
@Service
@Slf4j
public class ProductPackageServiceImpl implements ProductPackageService {

    @Autowired
    private BenefitMapper benefitMapper;

    @Autowired
    private ProductPackageMapper productPackageMapper;

    @Autowired
    private PackageBenefitMapper packageBenefitMapper;

    @Override
    public List<ProductPackageDTO> listPackages(String status) {

        LambdaQueryWrapper<ProductPackageDO> queryWrapper = new LambdaQueryWrapper<ProductPackageDO>();
        if(status != null){
            queryWrapper.eq(ProductPackageDO::getStatus, status);
        }
        queryWrapper.orderByAsc(ProductPackageDO::getSort);
        List<ProductPackageDO> productPackageDOList = productPackageMapper.selectList(queryWrapper);

        //将套餐填充权益信息
        return productPackageDOList.stream().map(productPackageDO -> {
            List<BenefitDTO> packageBenefits = getPackageBenefits(productPackageDO.getId());
            ProductPackageDTO dto = SpringBeanUtil.copyProperties(productPackageDO, ProductPackageDTO.class);
            dto.setBenefits(packageBenefits);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<BenefitDTO> getPackageBenefits(Long packageId) {
            LambdaQueryWrapper<PackageBenefitDO> queryWrapper = new LambdaQueryWrapper<PackageBenefitDO>();
            queryWrapper.eq(PackageBenefitDO::getProductPackageId, packageId);
            List<PackageBenefitDO> packageBenefitDOList = packageBenefitMapper.selectList(queryWrapper);
            //转换为DTO
        return packageBenefitDOList.stream().map(packageBenefitDO -> {
            BenefitDO benefitDO = benefitMapper.selectOne(new LambdaQueryWrapper<BenefitDO>()
                    .eq(BenefitDO::getBenefitCode, packageBenefitDO.getBenefitCode()));
            return SpringBeanUtil.copyProperties(benefitDO, BenefitDTO.class);
        }).collect(Collectors.toList());
    }
}
