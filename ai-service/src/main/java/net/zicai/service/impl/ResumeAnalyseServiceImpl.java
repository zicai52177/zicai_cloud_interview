package net.zicai.service.impl;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.ResumeAnalyseMessageDTO;
import net.zicai.dto.ResumeAnalysisResultDTO;
import net.zicai.enums.ResumeStatus;
import net.zicai.mapper.ResumeMapper;
import net.zicai.model.ResumeDO;
import net.zicai.service.BenefitTaskService;
import net.zicai.service.ResumeAnalyseService;
import net.zicai.util.JsonUtil;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

/**
 * @author wangdi
 * @date 2026/5/4 12:01
 * @description
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalyseServiceImpl implements ResumeAnalyseService {
    private final ChatModel chatModel;
    private final ToolCallbackProvider toolCallbackProvider;
    private final ResumeMapper resumeMapper;
    private final BenefitTaskService benefitTaskService;
    private ReactAgent reactAgent;
    private BeanOutputConverter<ResumeAnalysisResultDTO> convert;

    /**
     * 初始化 ReactAgent（Spring 容器启动后执行）
     */
    @PostConstruct
    public void init() {
        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();
        log.info("ReactAgent 初始化, 加载 {} 个 MCP 工具", toolCallbacks.length);

        convert = new BeanOutputConverter<>(ResumeAnalysisResultDTO.class);
        reactAgent = ReactAgent.builder()
                .name("mcp-react-agent")
                .model(chatModel)
                .tools(toolCallbacks)
                .outputSchema(convert.getFormat())
                .systemPrompt("你是一个简历分析专家，使用MCP服务进行分析用户的简历。请严格按照JSON Schema格式返回分析结果。")
                .build();
    }
    @Override
    public boolean processResumeAnalyse(ResumeAnalyseMessageDTO messageDTO) {

        Long resumeId = messageDTO.getResumeId();
        String messageId = messageDTO.getMessageId();
        String businessId = String.valueOf(resumeId);
        //获取简历
        ResumeDO resumeDO = resumeMapper.selectOne(new LambdaQueryWrapper<ResumeDO>().eq(ResumeDO::getId, resumeId).eq(ResumeDO::getAccountId, messageDTO.getAccountId()));
        if (resumeDO == null){
            log.warn("简历不存在, resumeId={}", resumeId);
            benefitTaskService.rollBack(messageId, businessId, "resume_analyse", "简历不存在");
            return false;
        }
        //调用MCP
        ResumeAnalysisResultDTO resumeAnalysisResultDTO = analyseResume(resumeDO);
        if(resumeAnalysisResultDTO == null){
            log.warn("简历分析失败, resumeId={}", resumeId);
            benefitTaskService.rollBack(messageId, businessId, "resume_analyse", "MCP调用异常");
            return false;
        }
        //更新结果
        resumeDO.setStatus(ResumeStatus.ANALYZED.name());
        resumeDO.setEvaluation(JsonUtil.obj2Json(resumeAnalysisResultDTO));
        resumeDO.setSkillTags(JsonUtil.obj2Json(resumeAnalysisResultDTO.getSkillTags()));
        resumeMapper.updateById(resumeDO);
        //标记任务完成
        boolean resumeAnalyse = benefitTaskService.markFinish(messageId, businessId, "resume_analyse");
        if(!resumeAnalyse){
            log.info("简历分析失败，等待延迟队列检查, resumeId={}", resumeId);
            return true;
        }
        log.info("简历分析成功, resumeId={}", resumeId);
        return true;
    }

    private ResumeAnalysisResultDTO analyseResume(ResumeDO resumeDO) {
        try {
            String result = reactAgent.call(resumeDO.getContent()).getText();
            log.debug("MCP返回: {}", result);
            return convert.convert(result);
        } catch (Exception e) {
            log.error("MCP调用异常, resumeId={}", resumeDO.getId(), e);
            return null;
        }
    }
}
