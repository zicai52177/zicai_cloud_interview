package net.xdclass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目DTO
 * 用于AI生成的题目信息传输
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO implements Serializable {

    private Long id;

    /**
     * 题目内容，采用Markdown格式
     */
    private String content;

    /**
     * 题目类型: SHORT_ANSWER, CODING
     */
    private String type;

    /**
     * 难度级别: BEGINNER, INTERMEDIATE, ADVANCED
     */
    private String difficulty;

    /**
     * 题目分类（如：Java基础、Spring框架、数据库等）
     */
    private String category;

    /**
     * 标准答案
     */
    private String standardAnswer;

    /**
     * 关键要点，JSON格式
     */
    private String keyPoints;

    /**
     * 满分分值
     */
    private Integer maxScore = 100;

    /**
     * 答题时间限制（分钟）
     */
    private Integer timeLimit;

    private Date gmtCreate;

    private Long interviewId;

    private Long interviewRoundId;

    private String userAnswer;

    private Integer userScore;

    private String evaluation;

    private String status;
}
