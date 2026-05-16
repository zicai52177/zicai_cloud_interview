package net.zicai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.controller.req.AnswerReq;
import net.zicai.controller.req.InterviewCreateReq;
import net.zicai.controller.req.InterviewFinishReq;
import net.zicai.controller.req.InterviewPageReq;
import net.zicai.dto.*;
import net.zicai.enums.BenefitEnum;
import net.zicai.enums.BizCodeEnum;
import net.zicai.enums.InterviewStatusEnum;
import net.zicai.feign.AccountBenefitFeign;
import net.zicai.interceptor.AccountLoginInterceptor;
import net.zicai.mapper.InterviewMapper;
import net.zicai.mapper.InterviewRoundMapper;
import net.zicai.mapper.QuestionMapper;
import net.zicai.mapper.ResumeMapper;
import net.zicai.model.InterviewDO;
import net.zicai.model.InterviewRoundDO;
import net.zicai.model.QuestionDO;
import net.zicai.model.ResumeDO;
import net.zicai.req.BenefitCheckReq;
import net.zicai.service.InterviewService;
import net.zicai.strategy.BenefitMessageStrategy;
import net.zicai.strategy.BenefitMessageStrategyFactory;
import net.zicai.util.CommonUtil;
import net.zicai.util.JsonData;
import net.zicai.util.JsonUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * AI面试服务实现类
 * <p>
 * 核心职责：
 * 1. 面试创建：权益校验 + MQ异步触发
 * 2. 面试生成：SequentialAgent编排(简历解析→轮次生成) + ParallelAgent并行题目生成
 * 3. 答题评估：异步AI评分 + 自动触发轮次评价
 * 4. 面试结束：汇总评估 + 生成综合报告
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewMapper interviewMapper;
    private final InterviewRoundMapper interviewRoundMapper;
    private final QuestionMapper questionMapper;
    private final ResumeMapper resumeMapper;
    private final ResumeDTO resumeDTO;
    private final AccountBenefitFeign benefitService;
    private final BenefitMessageStrategyFactory strategyFactory;

    @Override
    public JsonData create(InterviewCreateReq req) {

        //获取用户信息
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        if (accountDTO == null) {
            return JsonData.buildError("用户不存在");
        }
        ResumeDO resumeDO = resumeMapper.selectById(accountDTO.getId());
        if (resumeDO == null){
            return JsonData.buildError("请先上传简历");
        }
        //构建面试DO
        String interviewTitle = resumeDTO.getFilename() + "_" +
                String.format("岗位:%s-%s-期望薪资:%s",
                        req.getTargetPosition(),
                        BenefitEnum.valueOf(req.getInterviewType()).getTitle(),
                        req.getExpectedSalary());
        String description = String.format("⾯试轮次：%s ;当前 %s 经验⾯试；期望薪资:%s; 期望⼯作城市:%s;",
                req.getInterviewType(), req.getWorkYears(), req.getExpectedSalary(), req.getTargetCity());
        String profileJson = JsonUtil.obj2Json(req);
        InterviewDO interviewDO = InterviewDO.builder()
                .resumeId(req.getResumeId())
                .title(interviewTitle)
                .description(description)
                .type(req.getInterviewType())
                .status(InterviewStatusEnum.GENERATING.getCode())
                .accountId(accountDTO.getId())
                .extendContent(req.getSpecialContent())
                .profile(profileJson)
                .build();
        interviewMapper.insert(interviewDO);
        Long interviewId = interviewDO.getId();
        String businessId = interviewId.toString();
        log.info("创建⾯试记录成功, ⾯试ID={}, ⽤户ID={}", interviewId, accountDTO.getId());
        //扣减权益，如果扣减失败就删除掉面试订单
        BenefitCheckReq benefitCheckReq = new BenefitCheckReq(
                req.getInterviewType(), accountDTO.getId(), businessId, 1);
        JsonData jsonData = benefitService.checkAndDeductBenefit(benefitCheckReq);
        if (jsonData.getCode()!=0) {
            log.warn("权益扣减失败，⽤户：{}，权益编码：{}，错误：{}",
                    accountDTO.getId(), req.getInterviewType(), jsonData.getMsg());
            interviewMapper.deleteById(interviewId);
            return JsonData.buildResult(BizCodeEnum.BENEFIT_NOT_ENOUGH);
        }
        log.info("权益扣减成功, ⽤户ID={}", accountDTO.getId());
        // 4. 解析扣减结果
        BenefitCheckResultDTO deductResult = jsonData.getData(BenefitCheckResultDTO.class);
        log.info("权益扣减成功，messageId={}，开始发送业务MQ消息", deductResult.getMessageId());
        //发送MQ消息
        try {
            BenefitMessageStrategy strategy = strategyFactory.getStrategy(benefitCheckReq.getBenefitCode());
            strategy.sendBusinessMessage(
                    deductResult.getMessageId(),
                    deductResult.getBusinessId(),
                    benefitCheckReq.getAccountId()
            );
            log.info("业务MQ消息发送成功，messageId={}", deductResult.getMessageId());
        } catch (Exception e) {
            log.error("业务MQ消息发送失败，messageId={}，等待延迟检查补偿", deductResult.getMessageId(), e);
        }
        //返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("interviewId", interviewId);
        result.put("status", "generating");
        result.put("message", "⾯试正在⽣成中，请稍候...");
        return JsonData.buildSuccess(result);
    }

    @Override
    public void generateInterview(InterviewCreateMessageDTO messageDTO) {

    }

    @Override
    public JsonData getInterviewStatus(Long interviewId) {
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();

        InterviewDO interviewDO = findInterviewByIdAndAccount(interviewId, accountDTO.getId());
        if (interviewDO == null) {
            return JsonData.buildError("面试记录不存在");
        }
        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("interviewId", interviewId);
        statusInfo.put("status", interviewDO.getStatus());
        InterviewStatusEnum statusEnum = InterviewStatusEnum.getByCode(interviewDO.getStatus());
        statusInfo.put("statusName", statusEnum != null ? statusEnum.getDesc() : "未知状态");
        return JsonData.buildSuccess(statusInfo);
    }

    @Override
    public JsonData answer(AnswerReq req) {
        return null;
    }

    @Override
    public JsonData finishAsync(InterviewFinishReq req, Long accountId) {
        return null;
    }

    @Override
    public void finishAsyncInternal(InterviewFinishReq req, Long accountId) {

    }

    @Override
    public JsonData viewInterviewDetail(Long interviewId) {
        return null;
    }

    @Override
    public JsonData page(InterviewPageReq req) {
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();

        LambdaQueryWrapper<InterviewDO> queryWrapper = new LambdaQueryWrapper<InterviewDO>()
                .eq(InterviewDO::getAccountId, accountDTO.getId())
                .orderByDesc(InterviewDO::getGmtCreate);

        Page<InterviewDO> interviewPage = interviewMapper.selectPage(
                new Page<>(req.getPage(), req.getSize()), queryWrapper);
        Map<String, Object> result = CommonUtil.convertToPageMap(interviewPage, InterviewDTO.class);
        return JsonData.buildSuccess(result);
    }

    // ==================== 删除面试 ====================

    @Override
    public JsonData delete(Long interviewId) {
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();

        InterviewDO interviewDO = findInterviewByIdAndAccount(interviewId, accountDTO.getId());
        if (interviewDO == null) {
            return JsonData.buildError("面试记录不存在或无权限删除");
        }

        // 删除轮次和题目
        interviewRoundMapper.delete(new LambdaQueryWrapper<InterviewRoundDO>()
                .eq(InterviewRoundDO::getInterviewId, interviewId)
                .eq(InterviewRoundDO::getAccountId, accountDTO.getId()));
        questionMapper.delete(new LambdaQueryWrapper<QuestionDO>()
                .eq(QuestionDO::getInterviewId, interviewId)
                .eq(QuestionDO::getAccountId, accountDTO.getId()));
        interviewMapper.deleteById(interviewId);

        log.info("删除面试记录成功，面试ID：{}", interviewId);
        return JsonData.buildSuccess("面试记录删除成功");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据ID和账号查询面试记录（通用方法，避免重复编写查询逻辑）
     */
    private InterviewDO findInterviewByIdAndAccount(Long interviewId, Long accountId) {
        return interviewMapper.selectOne(
                new LambdaQueryWrapper<InterviewDO>()
                        .eq(InterviewDO::getId, interviewId)
                        .eq(InterviewDO::getAccountId, accountId));
    }

    /**
     * 更新面试状态
     */
    private void updateInterviewStatus(Long interviewId, InterviewStatusEnum statusEnum) {
        InterviewDO updateDO = InterviewDO.builder()
                .id(interviewId)
                .status(statusEnum.getCode())
                .description(statusEnum.getDesc())
                .build();
        interviewMapper.updateById(updateDO);
        log.info("更新面试状态，面试ID：{}，状态：{}", interviewId, statusEnum.getCode());
    }

    /**
     * 查询面试下所有轮次（按轮次号升序）
     */
    private List<InterviewRoundDO> getRoundsByInterview(Long interviewId, Long accountId) {
        return interviewRoundMapper.selectList(
                new LambdaQueryWrapper<InterviewRoundDO>()
                        .eq(InterviewRoundDO::getInterviewId, interviewId)
                        .eq(InterviewRoundDO::getAccountId, accountId)
                        .orderByAsc(InterviewRoundDO::getRoundNumber)
        );
    }

    /**
     * 查询面试下所有题目（按创建时间升序）
     */
    private List<QuestionDO> getQuestionsByInterview(Long interviewId, Long accountId) {
        return questionMapper.selectList(
                new LambdaQueryWrapper<QuestionDO>()
                        .eq(QuestionDO::getInterviewId, interviewId)
                        .eq(QuestionDO::getAccountId, accountId)
                        .orderByAsc(QuestionDO::getGmtCreate)
        );
    }
}