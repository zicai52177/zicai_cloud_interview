package net.xdclass.service;

import net.xdclass.controller.req.AnswerReq;
import net.xdclass.controller.req.InterviewCreateReq;
import net.xdclass.controller.req.InterviewFinishReq;
import net.xdclass.controller.req.InterviewPageReq;
import net.xdclass.dto.InterviewCreateMessageDTO;
import net.xdclass.util.JsonData;

/**
 * AI面试服务接口
 * <p>
 * 核心业务流程：
 * 1. 创建面试 {@link #create} → 权益扣减 + MQ异步触发生成
 * 2. 生成面试 {@link #generateInterview} → SequentialAgent编排(简历解析→轮次生成) + 并行题目生成
 * 3. 答题评估 {@link #answer} → 提交答案 + 异步AI评估
 * 4. 结束面试 {@link #finishAsync} → 汇总评估 + 生成综合报告
 * </p>
 */
public interface InterviewService {

    /**
     * 创建面试（权益扣减 + 发MQ异步生成）
     *
     * @param req 面试创建请求（含简历ID、目标岗位、期望薪资等）
     * @return 包含 interviewId 和 status 的结果
     */
    JsonData create(InterviewCreateReq req);

    /**
     * 生成面试内容（由MQ消费者调用）
     * <p>流程：SequentialAgent编排(简历解析→轮次生成) → 保存轮次 → 并行生成题目</p>
     *
     * @param messageDTO MQ消息体（含面试ID、用户ID、消息ID）
     */
    void generateInterview(InterviewCreateMessageDTO messageDTO);

    /**
     * 查询面试状态
     *
     * @param interviewId 面试ID
     * @return 面试状态信息（状态码、描述、题目数量等）
     */
    JsonData getInterviewStatus(Long interviewId);

    /**
     * 回答题目（更新答案 + 异步AI评估）
     *
     * @param req 答题请求（含题目ID、面试ID、用户答案）
     * @return 提交结果
     */
    JsonData answer(AnswerReq req);

    /**
     * 异步结束面试（入口方法）
     * <p>校验状态后立即返回，异步调用 {@link #finishAsyncInternal} 处理评估和报告生成</p>
     *
     * @param req       结束面试请求
     * @param accountId 用户ID
     * @return 处理结果
     */
    JsonData finishAsync(InterviewFinishReq req, Long accountId);

    /**
     * 异步结束面试内部逻辑（@Async执行）
     * <p>流程：处理未评估题目 → 等待评估完成 → 处理未作答题目 → 生成综合报告</p>
     *
     * @param req       结束面试请求
     * @param accountId 用户ID
     */
    void finishAsyncInternal(InterviewFinishReq req, Long accountId);

    /**
     * 查看面试详情（含轮次和题目）
     *
     * @param interviewId 面试ID
     * @return 面试详情（面试信息 + 轮次列表 + 题目列表）
     */
    JsonData viewInterviewDetail(Long interviewId);

    /**
     * 分页查询面试记录
     *
     * @param req 分页请求参数
     * @return 分页结果
     */
    JsonData page(InterviewPageReq req);

    /**
     * 删除面试记录（级联删除轮次和题目）
     *
     * @param interviewId 面试ID
     * @return 删除结果
     */
    JsonData delete(Long interviewId);
}
