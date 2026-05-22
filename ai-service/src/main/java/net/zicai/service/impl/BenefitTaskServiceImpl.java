package net.zicai.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.enums.BenefitTaskStateEnum;
import net.zicai.feign.AccountBenefitFeign;
import net.zicai.req.BenefitTaskUpdateReq;
import net.zicai.service.BenefitTaskService;
import net.zicai.util.JsonData;
import org.springframework.stereotype.Service;

/**
 * @author wangdi
 * @date 2026/5/4 12:13
 * @description
 */
/**
 * 权益任务状态服务实现类
 * 封装与account-service的Feign调用
 *
 * @author 小滴课堂
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BenefitTaskServiceImpl implements BenefitTaskService {

    private final AccountBenefitFeign accountBenefitFeign;

    @Override
    public boolean markFinish(String messageId, String businessId, String businessType) {
        return updateTaskStatus(messageId, businessId, businessType,
                BenefitTaskStateEnum.FINISH.getCode(), null);
    }

    @Override
    public boolean rollBack(String messageId,String businessId,String businessType,String failReason){
        return updateTaskStatus(messageId, businessId, businessType,
                BenefitTaskStateEnum.CANCEL.getCode(), failReason);
    }

    /**
     * 统一更新任务状态
     */
    private boolean updateTaskStatus(String messageId, String businessId,
                                     String businessType, String operation, String failReason) {
        try {
            BenefitTaskUpdateReq req = BenefitTaskUpdateReq.builder()
                    .messageId(messageId)
                    .businessId(businessId)
                    .businessType(businessType)
                    .operation(operation)
                    .failReason(failReason)
                    .build();

            JsonData result = accountBenefitFeign.updateTaskStatus(req);
            boolean success = result.getCode() == 0;

            log.info("任务状态更新: messageId={}, operation={}, result={}",
                    messageId, operation, success ? "成功" : "失败");

            return success;

        } catch (Exception e) {
            log.error("任务状态更新异常: messageId={}, operation={}, error={}",
                    messageId, operation, e.getMessage(), e);
            return false;
        }
    }
}
