package net.zicai.service;

import net.zicai.dto.BenefitDTO;

import java.util.List;

/**
 * 权益服务接口
 */
public interface BenefitService {

    /**
     * 查询权益列表
     *
     * @param status 状态（可选，ON上线/OFF下线）
     * @return BenefitDTO列表
     */
    List<BenefitDTO> list(String status);
}
