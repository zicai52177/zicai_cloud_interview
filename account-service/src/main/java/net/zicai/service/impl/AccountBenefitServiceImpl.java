package net.zicai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.BenefitDelayCheckMQConfig;
import net.zicai.dto.BenefitCheckResultDTO;
import net.zicai.dto.BenefitDelayCheckMessageDTO;
import net.zicai.enums.BizCodeEnum;
import net.zicai.mapper.AccountBenefitMapper;
import net.zicai.mapper.BenefitTaskMapper;
import net.zicai.model.AccountBenefitDO;
import net.zicai.model.BenefitTaskDO;
import net.zicai.req.BenefitCheckReq;
import net.zicai.service.AccountBenefitService;
import net.zicai.service.BenefitTaskService;
import net.zicai.util.JsonData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author wangdi
 * @date 2026/5/3 20:07
 * @description
 */
@Slf4j
@Service
public class AccountBenefitServiceImpl implements AccountBenefitService {

    @Autowired
    private AccountBenefitMapper accountBenefitMapper;
    @Autowired
    private BenefitTaskService benefitTaskService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 检查并扣减权益
     *
     * 设计说明：
     * - 权益扣减和任务保存在同一事务中，保证数据一致性
     * - 返回messageId供ai-service发送业务MQ使用
     *
     * 事务回滚 → MQ消息已发送 → 消费时查不到任务记录 → 跳过处理 ✓
     * 事务提交 → MQ消息已发送 → 消费时查到任务记录 → 正常处理 ✓
     * @param benefitCheckReq 权益检查请求
     * @return 扣减结果（包含messageId）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData checkAndDeductBenefit(BenefitCheckReq benefitCheckReq) {
        // 1. 查询可用权益（优先使用即将过期的）
        List<AccountBenefitDO> accountBenefitDOS = accountBenefitMapper.selectList(new LambdaQueryWrapper<AccountBenefitDO>()
                .eq(AccountBenefitDO::getAccountId, benefitCheckReq.getAccountId())
                .eq(AccountBenefitDO::getBenefitCode, benefitCheckReq.getBenefitCode())
                .eq(AccountBenefitDO::getRemainingCount, 0)
                .eq(AccountBenefitDO::getEndTime, new Date())
                .orderByDesc(AccountBenefitDO::getEndTime)
        );
        if (accountBenefitDOS.isEmpty()){
            log.warn("可扣减权益次数不足");
            return JsonData.buildResult(BizCodeEnum.BENEFIT_NOT_ENOUGH);
        }
        // 2. 创建权益任务记录（本地消息表，用于补偿机制）
        AccountBenefitDO accountBenefitDO = accountBenefitDOS.getFirst();
        int count = benefitCheckReq.getCount() != null ? benefitCheckReq.getCount() : 1;
        BenefitTaskDO benefitTaskDO = benefitTaskService.saveTask(
                benefitCheckReq.getBusinessId(),
                benefitCheckReq.getBenefitCode(),
                benefitCheckReq.getAccountId(),
                accountBenefitDO.getId(),
                count);
        // 3. 发送延迟检查MQ消息（优先发送，确保监控消息发出）
        sendDelayCheckMqMsg(benefitTaskDO.getMessageId(), benefitCheckReq.getBusinessId());
        // 如果MQ发送成功但后续本地操作失败 → 消费时查不到任务记录 → 跳过处理
        // 4. 扣减权益余量
        accountBenefitDO.setRemainingCount(accountBenefitDO.getRemainingCount() - count);
        accountBenefitMapper.updateById(accountBenefitDO);
        log.info("权益扣减成功");
        // 5. 构建返回结果（供ai-service使用）
        BenefitCheckResultDTO benefitCheckResultDTO = BenefitCheckResultDTO.builder()
                .success(true)
                .messageId(benefitTaskDO.getMessageId())
                .businessId(benefitCheckReq.getBusinessId())
                .benefitCode(benefitCheckReq.getBenefitCode())
                .accountBenefitId(accountBenefitDO.getId())
                .remainingCount(accountBenefitDO.getRemainingCount())
                .deductedCount(count    ).build();
        return JsonData.buildSuccess(benefitCheckResultDTO);
    }

    private void sendDelayCheckMqMsg(String messageId, String businessId) {

        //第一次检查
        BenefitDelayCheckMessageDTO firstDelayCheckMessageDTO = BenefitDelayCheckMessageDTO.builder()
                .messageId(messageId)
                .businessId(businessId)
                .checkLevel(1)
                .build();
        rabbitTemplate.convertAndSend(BenefitDelayCheckMQConfig.BENEFIT_DELAY_CHECK_EXCHANGE,
                BenefitDelayCheckMQConfig.DELAY_CHECK_ROUTING_KEY_1MIN,
                firstDelayCheckMessageDTO
                );
        log.info("第一次发送延迟检查MQ消息：{}", firstDelayCheckMessageDTO);
        //第二次检查
        BenefitDelayCheckMessageDTO secondDelayCheckMessageDTO = BenefitDelayCheckMessageDTO.builder()
                .messageId(messageId)
                .businessId(businessId)
                .checkLevel(2)
                .build();
        rabbitTemplate.convertAndSend(BenefitDelayCheckMQConfig.BENEFIT_DELAY_CHECK_EXCHANGE,
                BenefitDelayCheckMQConfig.DELAY_CHECK_ROUTING_KEY_5MIN,
                secondDelayCheckMessageDTO
        );
        log.info("第二次发送延迟检查MQ消息：{}", secondDelayCheckMessageDTO);
    }
}
