package net.zicai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 面试记录数据对象
 *
 * @author 小滴课堂-二当家小D,
 * @since 2026-01-14
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "InterviewDO", description = "")
public class InterviewDTO implements Serializable {


    private Long id;

    @Schema(description = "面试标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "面试类型，包括专项面、深度面、简历普通面")
    private String type;

    @Schema(description = "拓展字段")
    private String extendContent;

    @Schema(description = "用户求职画像")
    private String profile;

    @Schema(description = "账号ID")
    private Long accountId;

    @Schema(description = "简历ID")
    private Long resumeId;

    @Schema(description = "面试状态：GENERATING、IN_PROGRESS、EVALUATING")
    private String status;

    @Schema(description = "总体评分（0-100分）")
    private BigDecimal overallScore;

    @Schema(description = "面试总结和评价")
    private String summary;

    @Schema(description = "优势分析")
    private String strength;

    @Schema(description = "需要改进的地方")
    private String improvement;

    @Schema(description = "具体建议")
    private String suggestion;

    @Schema(description = "技术能力评分")
    private BigDecimal technicalSkills;

    @Schema(description = "逻辑思维评分")
    private BigDecimal logicalThinking;

    @Schema(description = "表达能力评分")
    private BigDecimal communication;

    @Schema(description = "解决问题能力评分")
    private BigDecimal problemSolving;

    @Schema(description = "及格题目数（>=60分）")
    private Integer passedQuestion;

    @Schema(description = "优秀题目数（>=80分）")
    private Integer excellentQuestion;

    private Date gmtCreate;

    private Date gmtModified;
}
