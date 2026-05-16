package net.xdclass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AI简历解析结果DTO
 * 从简历中提取关键结构化信息
 * 
 * @author 小滴课堂
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AIResumeInfoDTO", description = "AI简历解析结果")
public class AIResumeInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名称")
    private String userName;

    @Schema(description = "教育经历")
    private String education;

    @Schema(description = "工作年限")
    private String workYears;

    @Schema(description = "技术栈/技能标签")
    private List<String> skillTags;

    @Schema(description = "项目经验")
    private List<String> projectExperiences;

    @Schema(description = "简历原始全部内容")
    private String rawContent;
}
