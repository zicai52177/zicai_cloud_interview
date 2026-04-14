package net.zicai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.BannerDTO;
import net.zicai.enums.BizCodeEnum;
import net.zicai.exception.BizException;
import net.zicai.mapper.BannerMapper;
import net.zicai.model.BannerDO;
import net.zicai.service.BannerService;
import net.zicai.util.SpringBeanUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Banner服务实现类
 */
@Service
@Slf4j
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public BannerDTO findById(Long id) {
        log.info("查询Banner详情, id:{}", id);
        BannerDO bannerDO = bannerMapper.selectById(id);
        if (bannerDO == null) {
            log.warn("Banner不存在, id:{}", id);
            throw new BizException(BizCodeEnum.BANNER_NOT_EXIST);
        }
        return convertToDTO(bannerDO);
    }

    @Override
    public List<BannerDTO> list(String location, String status) {
        log.info("查询Banner列表, location:{}, status:{}", location, status);
        
        LambdaQueryWrapper<BannerDO> queryWrapper = new LambdaQueryWrapper<BannerDO>()
                .orderByDesc(BannerDO::getGmtCreate);
        
        // 动态添加查询条件
        if (StringUtils.isNotBlank(location)) {
            queryWrapper.eq(BannerDO::getLocation, location);
        }
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(BannerDO::getStatus, status);
        }
        
        List<BannerDO> bannerDOList = bannerMapper.selectList(queryWrapper);
        
        return bannerDOList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为DTO
     */
    private BannerDTO convertToDTO(BannerDO bannerDO) {
        return SpringBeanUtil.copyProperties(bannerDO, BannerDTO.class);
    }
}
