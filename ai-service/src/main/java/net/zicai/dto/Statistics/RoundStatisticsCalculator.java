package net.zicai.dto.Statistics;

/**
 * @author wangdi
 * @date 2026/5/17 15:28
 * @description
 */

import net.zicai.enums.RoundStatusEnum;
import net.zicai.model.InterviewRoundDO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 轮次统计计算器
 * 负责计算轮次相关的统计数据
 */
@Component
public class RoundStatisticsCalculator {
    /**
     * 计算轮次统计信息
     */
    public RoundStatistics calculate(List<InterviewRoundDO> rounds){
        BigDecimal totalScore = BigDecimal.ZERO;
        int totalQuestions = 0;
        int answeredQuestions = 0;
        int completedRounds = 0;
        for (InterviewRoundDO round : rounds) {
            if (round.getOverallScore() != null) {
                totalScore = totalScore.add(round.getOverallScore());
            }
            totalQuestions += round.getTotalQuestion() != null ? round.getTotalQuestion() : 0;
            answeredQuestions += round.getAnsweredQuestions() != null ? round.getAnsweredQuestions() : 0;
            if (RoundStatusEnum.COMPLETED.getCode().equals(round.getStatus())) {
                completedRounds++;
            }
        }
        BigDecimal averageScore = answeredQuestions > 0
                ? totalScore.divide(BigDecimal.valueOf(answeredQuestions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new RoundStatistics(totalQuestions, answeredQuestions, completedRounds, averageScore);
    }
    /**
     * 构建轮次评价信息
     */
    public String buildRoundsInfo(List<InterviewRoundDO> rounds) {
        StringBuilder roundsInfo = new StringBuilder();
        for (InterviewRoundDO round : rounds) {
            String roundSummary = round.getSummary() != null ? round.getSummary() : "未⽣成评价";
            String roundStrengths = round.getStrength() != null ? round.getStrength() : "未⽣成优势分析";
            String roundImprovements = round.getImprovement() != null ? round.getImprovement() : "未生成改进建议";
            String roundSuggestions = round.getSuggestion() != null ? round.getSuggestion() : "未生成学习建议";
            int score = round.getOverallScore() != null ? round.getOverallScore().intValue() : 0;
            int answered = round.getAnsweredQuestions() != null ? round.getAnsweredQuestions() : 0;
            int total = round.getTotalQuestion() != null ? round.getTotalQuestion() : 0;
            roundsInfo.append(String.format("""
                            第%d轮 - %s（%d分）
                            轮次描述：%s
                            题⽬情况：%d/%d题已作答
                            轮次评价：%s
                            优势分析：%s
                            改进建议：%s
                            学习建议：%s
                            
                            """,
                    round.getRoundNumber(),
                    round.getRoundType(),
                    score,
                    round.getDescription(),
                    answered,
                    total,
                    roundSummary,
                    roundStrengths,
                    roundImprovements,
                    roundSuggestions));
        }
        return roundsInfo.toString();
    }

}
