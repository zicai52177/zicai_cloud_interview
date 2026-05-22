package net.zicai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * AI面试轮次DTO
 * AI生成的面试轮次信息
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AIInterviewRoundDTO", description = "AI面试轮次DTO")
public class AIInterviewRoundDTO implements Serializable {

    @Schema(description = "第几轮面试，1、2、3、4")
    private Integer roundNumber;

    @Schema(description = "本轮面试类型，HR面、Java面、算法面、大数据面 等")
    private String roundType;

    @Schema(description = "本轮面试的主要考察内容，需要详细内容")
    private String description;

    @Schema(description = "面试总题目数量")
    private Integer totalQuestion;
}
