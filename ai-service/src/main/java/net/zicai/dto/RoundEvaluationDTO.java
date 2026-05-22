package net.zicai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 轮次评价DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoundEvaluationDTO {

    private Long roundId;
    private Long interviewId;
    private Long accountId;
    private Integer roundNumber;
    private String roundType;
    private BigDecimal averageScore;
    private Integer answeredQuestions;
    private Integer totalQuestions;

    /**
     * 轮次评价内容，采用markdown格式
     */
    private String evaluation;

    /**
     * 轮次优势分析，采用markdown格式
     */
    private String strengths;

    /**
     * 轮次不足分析，采用markdown格式
     */
    private String improvements;

    /**
     * 轮次学习建议，采用markdown格式
     */
    private String suggestions;

    private Date gmtCreate;
    private Date gmtModified;
}
