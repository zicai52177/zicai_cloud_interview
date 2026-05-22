package net.zicai.dto.Statistics;

/**
 * @author wangdi
 * @date 2026/5/17 15:02
 * @description
 */

import net.zicai.model.QuestionDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 题⽬统计计算器
 * 负责计算题⽬相关的统计数据
 */
@Component
public class QuestionStatisticsCalculator {
    /**
     * 构建题⽬统计信息
     */
    public QuestionStatistics buildStatistics(List<QuestionDO> answeredQuestions) {
        StringBuilder questionsInfo = new StringBuilder();
        int totalScore = 0;
        for (QuestionDO question : answeredQuestions) {
            questionsInfo.append(String.format("题⽬：%s\n回答：%s\n评分：%d分\n评价：%s\n\n",
                    question.getContent(),
                    question.getUserAnswer(),
                    question.getUserScore() != null ? question.getUserScore() : 0,
                    question.getEvaluation()));
            totalScore += question.getUserScore() != null ? question.getUserScore() : 0;
        }
        double averageScore = !answeredQuestions.isEmpty()
                ? (double) totalScore / answeredQuestions.size()
                : 0.0;
        return new QuestionStatistics(questionsInfo.toString(), averageScore);
    }
}