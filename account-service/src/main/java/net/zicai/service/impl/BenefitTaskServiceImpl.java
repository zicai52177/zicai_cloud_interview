package net.zicai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.zicai.enums.BenefitTaskStateEnum;
import net.zicai.mapper.AccountBenefitMapper;
import net.zicai.mapper.BenefitTaskMapper;
import net.zicai.model.AccountBenefitDO;
import net.zicai.model.BenefitTaskDO;
import net.zicai.req.BenefitTaskUpdateReq;
import net.zicai.service.BenefitTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author wangdi
 * @date 2026/5/3 19:59
 * @description
 */
@Slf4j
@Service
public class BenefitTaskServiceImpl implements BenefitTaskService {
    @Autowired
    private BenefitTaskMapper benefitTaskMapper;
    @Autowired
    private AccountBenefitMapper accountBenefitMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BenefitTaskDO saveTask(String businessId, String benefitCode, Long accountId, Long accountBenefitId, Integer useTimes) {
        // 生成唯一消息ID
        String messageId = UUID.randomUUID().toString();

        BenefitTaskDO task = BenefitTaskDO.builder()
                .messageId(messageId)
                .businessId(businessId)
                .benefitCode(benefitCode)
                .accountId(accountId)
                .accountBenefitId(accountBenefitId)
                .useTimes(useTimes)
                .lockState(BenefitTaskStateEnum.LOCK.name())
                .build();

        benefitTaskMapper.insert(task);
        log.info("保存权益任务成功, messageId={}, businessId={}", messageId, businessId);
        return task;
    }

    @Override
    public void checkAndCompensate(String messageId, Integer checkLevel) {
        //查询任务状态
        BenefitTaskDO task = benefitTaskMapper.selectOne(new LambdaQueryWrapper<BenefitTaskDO>()
                .eq(BenefitTaskDO::getMessageId, messageId));
        if (task == null) {
            log.warn("任务不存在, messageId={}", messageId);
            return;
        }
        //获取枚举值
        BenefitTaskStateEnum state = BenefitTaskStateEnum.valueOf(task.getLockState());
        //检查任务状态
        if(state == BenefitTaskStateEnum.FINISH){
            log.info("任务已完成, messageId={}", messageId);
        }
        if (state == BenefitTaskStateEnum.CANCEL){
            log.info("任务已取消, messageId={}", messageId);
        }
        if (checkLevel == 1 && state.isLock()) {
            log.info("第1次检查任务, messageId={}", messageId);

        }
        if (checkLevel == 2 && state.isLock()) {
            log.info("第2次检查依旧未成功，开始补偿, messageId={}", messageId);
            compensateBenefit(task);

        }

    }

    private void compensateBenefit(BenefitTaskDO task) {
        log.info("开始补偿, messageId={}", task.getMessageId());
        AccountBenefitDO accountBenefitDO =accountBenefitMapper.selectById(task.getAccountBenefitId());
        accountBenefitDO.setRemainingCount(accountBenefitDO.getRemainingCount() + task.getUseTimes());
        accountBenefitMapper.updateById(accountBenefitDO);
        log.info("补偿成功, messageId={}", task.getMessageId());
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTaskStatus(BenefitTaskUpdateReq req) {
        // 1. 查询任务记录
        BenefitTaskDO task = benefitTaskMapper.selectOne(
                new LambdaQueryWrapper<BenefitTaskDO>()
                        .eq(BenefitTaskDO::getMessageId, req.getMessageId())
        );

        if (task == null) {
            log.warn("任务记录不存在, messageId={}", req.getMessageId());
            return false;
        }

        BenefitTaskStateEnum currentState = BenefitTaskStateEnum.valueOf(task.getLockState());
        log.info("更新任务状态, messageId={}, businessId={}, 当前状态={}, 操作={}",
                req.getMessageId(), req.getBusinessId(), currentState.getDesc(), req.getOperation());

        // 2. 如果已经是FINISH或CANCEL状态，不允许再修改
        if (currentState.isFinish()) {
            log.info("任务已完成，无需更新, messageId={}", req.getMessageId());
            return true; // 已完成视为成功
        }

        if (currentState.isCancel()) {
            log.info("任务已取消，无需更新, messageId={}", req.getMessageId());
            return true; // 已取消视为成功
        }

        // 3. 根据操作类型更新状态
        if (BenefitTaskStateEnum.FINISH.getCode().equals(req.getOperation())) {
            // 标记为完成
            task.setLockState(BenefitTaskStateEnum.FINISH.name());
            benefitTaskMapper.updateById(task);
            log.info("任务状态更新为FINISH, messageId={}, businessId={}",
                    req.getMessageId(), req.getBusinessId());
            return true;

        } else if (BenefitTaskStateEnum.CANCEL.getCode().equals(req.getOperation())) {
            // 取消并回滚权益
            compensateBenefit(task);

            // 更新任务状态为CANCEL
            task.setLockState(BenefitTaskStateEnum.CANCEL.name());
            benefitTaskMapper.updateById(task);

            log.info("任务状态更新为CANCEL，权益已回滚, messageId={}, businessId={}, 原因={}",
                    req.getMessageId(), req.getBusinessId(), req.getFailReason());
            return true;

        } else {
            log.warn("未知的操作类型: {}, messageId={}", req.getOperation(), req.getMessageId());
            return false;
        }
    }
}
