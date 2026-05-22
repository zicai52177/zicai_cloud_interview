package net.zicai.component.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.zicai.controller.req.AnswerReq;
import net.zicai.dto.AIAnswerDTO;
import net.zicai.enums.QuestionStatusEnum;
import net.zicai.mapper.QuestionMapper;
import net.zicai.model.QuestionDO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author wangdi
 * @date 2026/5/17 13:51
 * @description
 */
@Component
@Slf4j
public class AnswerEvaluationAgent {

    private final ChatClient chatClient;

    @Autowired(required = false)
    private RoundEvaluationAgent roundEvaluationAgent;
    @Autowired
    private QuestionMapper questionMapper;

    public AnswerEvaluationAgent(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }


    @Async("aiInterviewExecutor")
    public void evaluateAsync(AnswerReq answerReq,Long accountId) {

        try {
            QuestionDO questionDO = questionMapper.selectOne(new LambdaQueryWrapper<QuestionDO>()
                    .eq(QuestionDO::getId, answerReq.getQuestionId())
                    .eq(QuestionDO::getAccountId, accountId)
            );
            if(questionDO == null){
                log.error("题目不存在，题目ID：{}", answerReq.getQuestionId());
                //更新状态
                updateQuestion(answerReq.getQuestionId(),accountId,answerReq.getAnswer());
                return;
            }

            //更新状态为开始评估
            questionMapper.update(QuestionDO.builder().id(answerReq.getQuestionId()).status(QuestionStatusEnum.EVALUATING.getCode()).build(),
                    new LambdaQueryWrapper<QuestionDO>()
                            .eq(QuestionDO::getId, answerReq.getQuestionId())
                            .eq(QuestionDO::getAccountId, accountId));

            //构建提示词
            String prompt = buildPrompt(questionDO, answerReq.getAnswer());

            //调用OpenAI
            AIAnswerDTO response = this.chatClient.prompt().user(prompt).call().entity(AIAnswerDTO.class);
            if (response == null){
                log.info("大模型返回为控:问题ID{}", answerReq.getQuestionId());
                //更新为已评估
                updateQuestion(answerReq.getQuestionId(),accountId,answerReq.getAnswer());
                return;
            }
            log.info("评估已完成，结果为: {}", response);
            //保存评估结果
            questionMapper.updateById(QuestionDO.builder()
                    .id(answerReq.getQuestionId())
                    .status(QuestionStatusEnum.COMPLETED.getCode())
                    .evaluation(response.getEvaluation())
                    .userScore(response.getScore())
                    .userAnswer(answerReq.getAnswer())
                    .build());
            //检查是否是本轮最后一个题目
            if(roundEvaluationAgent != null && questionDO.getInterviewRoundId() != null){
                roundEvaluationAgent.checkAndGenerate(questionDO.getInterviewRoundId(), accountId,answerReq.getInterviewId());
            }


        } catch (Exception e) {
            log.error("AnswerEvaluationAgent evaluateAsync error", e);
            updateQuestion(answerReq.getQuestionId(),accountId,answerReq.getAnswer());
        }


    }

    /**
     * 处理错误时也更新为已评估
     * @param questionId
     * @param accountId
     */
    private void updateQuestion(Long questionId, Long accountId,String userAnswer) {
        questionMapper.update(QuestionDO.builder().id(questionId).status(QuestionStatusEnum.ANSWERED.getCode()).userAnswer(userAnswer).build(), new LambdaQueryWrapper<QuestionDO>()
                .eq(QuestionDO::getId, questionId)
                .eq(QuestionDO::getAccountId, accountId)
        );
    }


    /**
     * 构建答案评估提示词（6维度评分体系，总计100分）
     */
    private String buildPrompt(QuestionDO questionDO, String answer) {
        return String.format("""
                        作为⼀名资深技术⾯试官，请对以下⾯试回答进⾏全⾯的多维度分析和评估。
                        
                        **⾯试题⽬：**
                        %s
                        
                        **应聘者回答：**
                        %s
                        
                        **参考答案：**
                        %s
                        
                        **评估要求：**
                        请从以下多个维度进⾏深⼊分析，并提供详细的评价报告：
                        
                        **1. 技术准确性分析（25分）**
                        - 技术概念理解是否正确
                        - 技术细节掌握程度
                        - 最重要回答内容需要和题⽬强相关且完整内容，如果简单⼏个关键词或者不相⼲回答则10分以内
                        
                        **2. 实际应⽤能⼒（20分）**
                        - 能否结合实际⼯作场景
                        - 解决⽅案的实⽤性
                        - 最重要回答内容需要和题⽬强相关且完整内容，如果简单⼏个关键词或者不相⼲回答则10分以内
                        
                        **3. 问题解决思路（20分）**
                        - 分析问题的逻辑性
                        - 解决思路的清晰度
                        - 最重要回答内容需要和题⽬强相关且完整内容，如果简单⼏个关键词或者不相⼲回答则10分以内
                        
                        **4. 代码质量（15分，如适⽤）**
                        - 代码规范性
                        - 算法效率
                        - 最重要回答内容需要和题⽬强相关且完整内容，如果简单⼏个关键词或者不相⼲回答则5分以内
                         **5. 沟通表达能⼒（10分）**
                        - 表达是否清晰
                        - 逻辑结构是否合理
                        - 最重要回答内容需要和题⽬强相关且完整内容，如果简单⼏个关键词或者不相⼲回答则2分以内                    
                        
                       **6. 学习能⼒和潜⼒（10分）**
                        - 对新技术的敏感度
                        - ⾃我反思能⼒
                        - 最重要回答内容需要和题⽬强相关且完整内容，如果简单⼏个关键词或者不相⼲回答则2分以内
                        
                       **输出格式要求：**
                        
                        **总体评分：** [0-100分]
                        
                        **综合评价：**                    
                         [整体评价和职业发展建议]
                                           
                       并把上述的每个要求整理出来全部内容采⽤markdown格式输出，存储到 evaluation 字段
                        
                        """,
                questionDO.getContent(), answer, questionDO.getStandardAnswer());
    }


}
