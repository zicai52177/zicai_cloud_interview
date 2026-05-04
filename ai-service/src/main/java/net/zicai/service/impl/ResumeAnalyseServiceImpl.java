package net.zicai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.ResumeAnalyseMessageDTO;
import net.zicai.mapper.ResumeMapper;
import net.zicai.model.ResumeDO;
import net.zicai.service.BenefitTaskService;
import net.zicai.service.ResumeAnalyseService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ResumeMapper resumeMapper;
    @Autowired
    private BenefitTaskService benefitTaskService;
    @Override
    public boolean processResumeAnalyse(ResumeAnalyseMessageDTO messageDTO) {

        Long resumeId = messageDTO.getResumeId();
        String messageId = messageDTO.getMessageId();
        String businessId = String.valueOf(resumeId);
        //获取简历
        ResumeDO resumeDO = resumeMapper.selectOne(new LambdaQueryWrapper<ResumeDO>().eq(ResumeDO::getId, resumeId).eq(ResumeDO::getAccountId, messageDTO.getAccountId()));
        if (resumeDO == null){
            log.warn("简历不存在, resumeId={}", resumeId);
            return false;
        }
        //调用MCP
        //更新结果
        //标记任务完成
        boolean resumeAnalyse = benefitTaskService.markFinish(messageId, businessId, "resume_analyse");
        if(!resumeAnalyse){
            log.info("简历分析失败，等待延迟队列检查, resumeId={}", resumeId);
            return true;
        }
        log.info("简历分析成功, resumeId={}", resumeId);
        return true;
    }
}
