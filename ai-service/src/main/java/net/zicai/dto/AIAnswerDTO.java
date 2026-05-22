package net.zicai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI答案评估结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnswerDTO implements Serializable {

    /**
     * 100分制，60分及格
     */
    private Integer score;

    /**
     * 针对用户的回答点评，采用markdown格式
     */
    private String evaluation;

    /**
     * 标准参考答案
     */
    private String standardAnswer;
}
