package net.zicai.service;

import net.zicai.dto.ResumeAnalyseMessageDTO;

/**
 * @author wangdi
 * @date 2026/5/4 12:01
 * @description
 */
public interface ResumeAnalyseService {

    boolean processResumeAnalyse(ResumeAnalyseMessageDTO messageDTO);
}
