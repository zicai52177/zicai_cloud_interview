package net.zicai.service;

import net.zicai.dto.ResumeDTO;
import net.zicai.util.JsonData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author wangdi
 * @date 2026/5/1 09:32
 * @description
 */
public interface ResumeService {


    public ResumeDTO upLodeAndParseFile(MultipartFile file);

    /**
     * 查询当前用户的简历列表
     * @return
     */
    List<ResumeDTO> list();

    /**
     * 查询简历详情
     * @param id
     * @return
     */
    ResumeDTO findById(Long id);

    /**
     * 删除简历
     * @param id
     * @return
     */
    boolean deleteById(Long id);

    JsonData getPreviewUrl(Long id);

    JsonData analyse(Long id);
}
