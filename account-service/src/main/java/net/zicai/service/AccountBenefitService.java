package net.zicai.service;

import net.zicai.req.BenefitCheckReq;
import net.zicai.util.JsonData;

/**
 * @author wangdi
 * @date 2026/5/3 20:09
 * @description
 */
public interface AccountBenefitService {
    JsonData checkAndDeductBenefit(BenefitCheckReq benefitCheckReq);
}
