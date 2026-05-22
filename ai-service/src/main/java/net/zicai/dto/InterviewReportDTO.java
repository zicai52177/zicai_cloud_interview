package net.zicai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 面试报告DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewReportDTO {

    /**
     * 总体评分（0-100分）
     */
    private BigDecimal overallScore;

    /**
     * 面试总结和评价，采用markdown格式
     */
    private String summary;

    /**
     * 优势分析，采用markdown格式
     */
    private String strengths;

    /**
     * 需要改进的地方，采用markdown格式
     */
    private String improvements;

    /**
     * 具体建议，采用markdown格式
     */
    private String suggestions;

    /**
     * 各个维度的评分
     */
    private DimensionScores dimensionScores;

    /**
     * 答题统计
     */
    private AnswerStatistics answerStatistics;

    /**
     * 每个题目的详细问答记录
     */
    private List<QuestionDTO> questionDTOList;

    /**
     * 维度评分
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionScores {
        private BigDecimal technicalSkills;
        private BigDecimal logicalThinking;
        private BigDecimal communication;
        private BigDecimal problemSolving;
    }

    /**
     * 答题统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerStatistics {
        private Integer totalQuestions;
        private Integer answeredQuestions;
        private BigDecimal averageScore;
        private Integer passedQuestions;
        private Integer excellentQuestions;
    }
}
