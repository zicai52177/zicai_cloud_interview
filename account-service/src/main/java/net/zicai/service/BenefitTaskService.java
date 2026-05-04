package net.zicai.service;

import net.zicai.model.BenefitTaskDO;
import net.zicai.req.BenefitTaskUpdateReq;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wangdi
 * @date 2026/5/3 19:59
 * @description
 */
public interface BenefitTaskService {

    @Transactional(rollbackFor = Exception.class)
    BenefitTaskDO saveTask(String businessId, String benefitCode,
                           Long accountId, Long accountBenefitId, Integer useTimes);

    void checkAndCompensate(String messageId, Integer checkLevel);

    boolean updateTaskStatus(BenefitTaskUpdateReq req);

}
