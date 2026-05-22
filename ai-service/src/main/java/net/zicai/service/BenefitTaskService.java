package net.zicai.service;

/**
 * @author wangdi
 * @date 2026/5/4 12:13
 * @description
 */
public interface BenefitTaskService {
    boolean markFinish(String messageId,String businessId,String businessType);
    boolean rollBack(String messageId,String businessId,String businessType,String failReason);

}
