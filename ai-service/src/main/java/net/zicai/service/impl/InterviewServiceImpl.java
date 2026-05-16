package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.controller.req.AnswerReq;
import net.xdclass.controller.req.InterviewCreateReq;
import net.xdclass.controller.req.InterviewFinishReq;
import net.xdclass.controller.req.InterviewPageReq;
import net.xdclass.dto.AccountDTO;
import net.xdclass.dto.InterviewCreateMessageDTO;
import net.xdclass.dto.InterviewDTO;
import net.xdclass.enums.InterviewStatusEnum;
import net.xdclass.interceptor.AccountLoginInterceptor;
import net.xdclass.mapper.InterviewMapper;
import net.xdclass.mapper.InterviewRoundMapper;
import net.xdclass.mapper.QuestionMapper;
import net.xdclass.model.InterviewDO;
import net.xdclass.model.InterviewRoundDO;
import net.xdclass.model.QuestionDO;
import net.xdclass.service.InterviewService;
import net.xdclass.util.CommonUtil;
import net.xdclass.util.JsonData;
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

    @Override
    public JsonData create(InterviewCreateReq req) {
        return null;
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