package net.zicai.component.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.AIAnswerDTO;
import net.zicai.dto.RoundEvaluationDTO;
import net.zicai.dto.Statistics.QuestionStatistics;
import net.zicai.dto.Statistics.QuestionStatisticsCalculator;
import net.zicai.dto.Statistics.RoundStatisticsCalculator;
import net.zicai.enums.RoundStatusEnum;
import net.zicai.mapper.InterviewRoundMapper;
import net.zicai.mapper.QuestionMapper;
import net.zicai.model.InterviewRoundDO;
import net.zicai.model.QuestionDO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author wangdi
 * @date 2026/5/17 13:52
 * @description
 */
@Component
@Slf4j
public class RoundEvaluationAgent {

    private final ChatClient chatClient;

    private final RoundStatisticsCalculator roundStatisticsCalculator;

    @Autowired
    private InterviewRoundMapper interviewRoundMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private QuestionStatisticsCalculator questionStatisticsCalculator;


    public RoundEvaluationAgent(ChatClient.Builder builder, RoundStatisticsCalculator roundStatisticsCalculator) {
        this.chatClient = builder.build();
        this.roundStatisticsCalculator = roundStatisticsCalculator;
    }

    public void checkAndGenerate(Long interviewRoundId, Long accountId, Long interviewId) {
        try {
            //判断这个轮次是否完成
            InterviewRoundDO roundDO = interviewRoundMapper.selectById(interviewRoundId);
            if (roundDO != null && !roundDO.getStatus().equals(RoundStatusEnum.COMPLETED.getCode())) {
                log.info("轮次已经评价完成，跳过{}", interviewRoundId);
                return;
            }
            //查询这个轮次中所有已评估的题目
            List<QuestionDO> questionDOList = questionMapper.selectList(new LambdaQueryWrapper<QuestionDO>()
                    .eq(QuestionDO::getInterviewRoundId, interviewRoundId)
                    .eq(QuestionDO::getStatus, RoundStatusEnum.COMPLETED.getCode()));
            List<QuestionDO> completedQuestionList = questionDOList.stream().filter(questionDO -> questionDO.getStatus().equals(RoundStatusEnum.COMPLETED.getCode())).toList();
            //如果完成就异步分析
            if (completedQuestionList.size() == roundDO.getTotalQuestion()) {
                log.info("轮次已经完成，开始分析{}", interviewRoundId);
                generateAsyck(roundDO, interviewId, accountId, completedQuestionList);
            } else {
                log.info("轮次未完成，继续等待{}", interviewRoundId);
            }
        } catch (Exception e) {
            log.error("", e);
        }


    }

    /**
     * 异步⽣成轮次评价
     * <p>流程：更新状态为评估中 → 统计题⽬数据 → AI⽣成评价 → 保存评价结果</p>
     *
     * @param roundDO               轮次对象（由 checkAndGenerate 直接传递，避免重复查询）
     * @param interviewId           ⾯试ID
     * @param accountId             ⽤户ID
     * @param completedQuestionList 已评估完成的题⽬列表
     */
    @Async("aiInterviewExecutor")
    public void generateAsyck(InterviewRoundDO roundDO, Long interviewId, Long accountId, List<QuestionDO> completedQuestionList) {

        log.info("开始分析轮次{}", roundDO.getId());
        //更新轮次状态为评估中
        updateRoundStatus(roundDO.getId(), RoundStatusEnum.EVALUATING);
        //构建题目统计信息
        QuestionStatistics questionStatistics = questionStatisticsCalculator.buildStatistics(completedQuestionList);
        //构建提示词
        String pmt = buildPrompt(roundDO, completedQuestionList.size(), questionStatistics);
        //调用大模型评估轮次
        RoundEvaluationDTO entity = chatClient.prompt().user(pmt).call().entity(RoundEvaluationDTO.class);
        log.info("轮次分析完成{}", roundDO.getId());
        //保存数据库轮次评价
        if(entity != null){
            interviewRoundMapper.updateById(InterviewRoundDO.builder()
                    .id(roundDO.getId())
                    .overallScore(BigDecimal.valueOf(questionStatistics.getAverageScore()))
                    .answeredQuestions(completedQuestionList.size())
                    .summary(entity.getEvaluation())
                    .strength(entity.getStrengths())
                    .improvement(entity.getImprovements())
                    .suggestion(entity.getSuggestions())
                    .status(RoundStatusEnum.COMPLETED.getCode())
                    .build());
        }else {
            log.info("轮次分析失败{}", roundDO.getId());
            updateRoundStatus(roundDO.getId(), RoundStatusEnum.FAILED);
        }


    }

    private void updateRoundStatus(Long roundId, RoundStatusEnum status) {
        interviewRoundMapper.updateById(InterviewRoundDO.builder().id(roundId).status(status.getCode()).build());
    }

    /**
     * 构建轮次评价提示词，从技术能⼒、问题解决、沟通表达、学习潜⼒4个维度评价
     */
    private String buildPrompt(InterviewRoundDO roundDO, int answeredCount, QuestionStatistics stats) {
        return String.format("""
                    作为⼀名资深技术⾯试官，请基于以下轮次⾯试情况，⽣成全⾯的轮次评价报告。
                    
                    **轮次基本信息：**
                    - 轮次序号：第%d轮
                    - 轮次类型：%s
                    - 轮次描述：%s
                    - 总题⽬数：%d
                    - 已回答题⽬数：%d
                    - 平均分数：%.1f分
                    
                    **详细题⽬回答情况：**
                    %s
                    
                    **轮次评价要求：**
                    请从以下维度进⾏综合评价：
                    
                    **1. 技术能⼒评估**
                    - 技术知识掌握程度
                    - 实际应⽤能⼒
                    - 技术深度和⼴度
                    
                    **2. 问题解决能⼒**
                    - 分析问题的逻辑性
                    - 解决⽅案的合理性
                    
                    **3. 沟通表达能⼒**
                    - 表达清晰度
                    - 逻辑结构
                    
                    **4. 学习成⻓潜⼒**
                    - 学习能⼒
                    - 持续改进意识
                    
                    **输出格式要求：**
                    
                    **轮次总体评分：** [0-100分]
                    
                    **轮次优势分析：strengths**
                    [详细分析该轮次中表现突出的⽅⾯，采⽤MarkDown格式]
                    
                    **轮次不⾜分析：improvements**
                    [详细深⼊指出该轮次中存在的问题，采⽤MarkDown格式]

                    **轮次学习建议：suggestions**
                    [针对该轮次表现的详细具体学习建议，采⽤MarkDown格式]

                    **综合评价：evaluation**
                    [详细深⼊综合评价，采⽤MarkDown格式]

                    请确保评价客观、专业，既要肯定优势，也要指出不⾜，并提供建设性的改进建议。
                    """,
                roundDO.getRoundNumber(),
                roundDO.getRoundType(),
                roundDO.getDescription(),
                roundDO.getTotalQuestion(),
                answeredCount,
                stats.getAverageScore(),
                stats.getQuestionsInfo());
    }

    }
