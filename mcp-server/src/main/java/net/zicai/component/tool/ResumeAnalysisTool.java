package net.zicai.component.tool;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.component.prompt.ResumePromptBuilder;
import net.zicai.dto.ResumeAnalysisResultDTO;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author wangdi
 * @date 2026/5/5 10:02
 * @description
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ResumeAnalysisTool {

    private final ChatModel chatModel;

    private final ResumePromptBuilder resumePromptBuilder;



    @McpTool(name="简历分析工具", description = "分析简历内容，提取技能标签、评估程序员级别、判断岗位匹配度")
    public String analyzeResume(@McpToolParam(description = "简历内容") String resumeContent) {
        log.info("开始调用工具分析简历内容：{}", resumeContent);
        if (resumeContent == null|| resumeContent.isEmpty()){
            log.warn("简历内容为空，无法分析");
            return buildEmptyResult().toString();
        }
        try {
            //创建convert
            BeanOutputConverter<ResumeAnalysisResultDTO> convert = new BeanOutputConverter<>(ResumeAnalysisResultDTO.class);
            String schema = convert.getFormat();
            String prompt = resumePromptBuilder.buildPrompt(resumeContent);
            ReactAgent build = ReactAgent.builder()
                    .model(chatModel)
                    .outputSchema(schema)
                    .systemPrompt(prompt)
                    .build();
            AssistantMessage call = build.call(resumeContent);
            return call.getText();
        } catch (Exception e) {
            log.error("工具调用异常：{}", e.getMessage(), e);
            return buildEmptyResult().toString();
        }
    }

    private ResumeAnalysisResultDTO buildEmptyResult(){
        return ResumeAnalysisResultDTO.builder()
                .basicPosition(ResumeAnalysisResultDTO.BasicPosition.builder()
                        .level(ResumeAnalysisResultDTO.ProgrammerLevel.JUNIOR)
                        .levelJudgmentBasis("简历内容为空，无法判断级别")
                        .positionFitDegree("未知")
                        .potentialGap("未知")
                        .build())
                .skillTags(Collections.emptyList())
                .evaluation("简历内容为空，无法进行分析")
                .build();
    }

}
