package net.zicai.service;

import net.zicai.dto.BannerDTO;

import java.util.List;

/**
 * Banner服务接口
 */
public interface BannerService {

    /**
     * 根据ID查询Banner详情
     *
     * @param id Banner ID
     * @return BannerDTO
     */
    BannerDTO findById(Long id);

    /**
     * 查询Banner列表
     *
     * @param location 位置（可选）
     * @param status   状态（可选）
     * @return BannerDTO列表
     */
    List<BannerDTO> list(String location, String status);
}
