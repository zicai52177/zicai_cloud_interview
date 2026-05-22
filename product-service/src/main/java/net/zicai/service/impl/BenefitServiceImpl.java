package net.zicai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.BenefitDTO;
import net.zicai.enums.StatusEnum;
import net.zicai.mapper.BenefitMapper;
import net.zicai.model.BenefitDO;
import net.zicai.service.BenefitService;
import net.zicai.util.SpringBeanUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权益服务实现类
 */
@Service
@Slf4j
public class BenefitServiceImpl implements BenefitService {

    @Autowired
    private BenefitMapper benefitMapper;

    @Override
    public List<BenefitDTO> list(String status) {
        log.info("查询权益列表, status:{}", status);

        LambdaQueryWrapper<BenefitDO> queryWrapper = new LambdaQueryWrapper<BenefitDO>()
                // 默认过滤状态为有效的权益
                .eq(BenefitDO::getStatus, StringUtils.isNotBlank(status) ? status : StatusEnum.ON.name())
                .orderByAsc(BenefitDO::getSort)
                .orderByDesc(BenefitDO::getGmtCreate);

        List<BenefitDO> benefitDOList = benefitMapper.selectList(queryWrapper);
        log.info("查询权益列表完成, count:{}", benefitDOList.size());

        return SpringBeanUtil.copyProperties(benefitDOList, BenefitDTO.class);
    }

    /**
     * DO转DTO
     *
     * @param benefitDO 权益实体
     * @return BenefitDTO
     */
    private BenefitDTO convertToDTO(BenefitDO benefitDO) {
        return SpringBeanUtil.copyProperties(benefitDO, BenefitDTO.class);
    }
}
