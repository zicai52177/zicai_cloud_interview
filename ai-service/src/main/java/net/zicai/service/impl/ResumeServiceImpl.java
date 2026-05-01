package net.zicai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.AccountDTO;
import net.zicai.dto.ResumeDTO;
import net.zicai.enums.ResumeStatus;
import net.zicai.enums.BizCodeEnum;
import net.zicai.exception.BizException;
import net.zicai.interceptor.AccountLoginInterceptor;
import net.zicai.mapper.ResumeMapper;
import net.zicai.model.ResumeDO;
import net.zicai.service.OssService;
import net.zicai.service.ResumeService;
import net.zicai.util.FileUtil;
import net.zicai.util.JsonData;
import net.zicai.util.SpringBeanUtil;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangdi
 * @date 2026/5/1 09:33
 * @description
 */
@Slf4j
@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeMapper resumeMapper;
    @Autowired
    private OssService ossService;
    @Autowired
    private AccountLoginInterceptor accountLoginInterceptor;

    /**
     * 上传简历并解析
     * @param file
     * @return
     */
    @Override
    public ResumeDTO upLodeAndParseFile(MultipartFile file) {

        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        //验证文件
        FileUtil.validateFile(file);
        //获取文件的类型
        String fileType = FileUtil.getFileType(file.getOriginalFilename());
        String content = null;
        //解析文件
        try{
            content = extractTextFromMultipartFile(file,fileType);
        }catch (Exception e){
            log.error("解析文件失败",e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR, e);
        }
        //上传文件
        String fileUrl = ossService.uploadSecureFile(file, "resume");
        log.info("上传文件成功，{}", fileUrl);
        //写入数据库
        ResumeDO resumeDO = ResumeDO.builder()
                .accountId(accountDTO.getId())
                .filename(file.getOriginalFilename())
                .filePath(fileUrl)
                .fileType(fileType)
                .content(content)
                .status(ResumeStatus.UPLOADED.name())
                .build();
        resumeMapper.insert(resumeDO);
        log.info("存入数据库成功，{}",resumeDO);
        return SpringBeanUtil.copyProperties(resumeDO, ResumeDTO.class);
    }

    /**
     * 查询当前用户的简历列表
     */
    @Override
    public List<ResumeDTO> list() {
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        List<ResumeDO> resumeDOList = resumeMapper.selectList(
                new LambdaQueryWrapper<ResumeDO>()
                        .eq(ResumeDO::getAccountId, accountDTO.getId())
                        .orderByDesc(ResumeDO::getGmtCreate)
        );
        return SpringBeanUtil.copyProperties(resumeDOList, ResumeDTO.class);
    }

    /**
     * 查询简历详情
     */
    @Override
    public ResumeDTO findById(Long id) {
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        ResumeDO resumeDO = resumeMapper.selectOne(
                new LambdaQueryWrapper<ResumeDO>()
                        .eq(ResumeDO::getId, id)
                        .eq(ResumeDO::getAccountId, accountDTO.getId())
        );
        if (resumeDO == null) {
            throw new BizException(BizCodeEnum.RESUME_NOT_EXIST);
        }
        return convertToDTO(resumeDO);
    }

    /**
     * 删除简历
     */
    @Override
    public boolean deleteById(Long id) {
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        LambdaQueryWrapper<ResumeDO> eq = new LambdaQueryWrapper<ResumeDO>()
                .eq(ResumeDO::getId, id)
                .eq(ResumeDO::getAccountId, accountDTO.getId());
        ResumeDO resumeDO = resumeMapper.selectOne(eq);
        if (resumeDO == null) {
            throw new BizException(BizCodeEnum.RESUME_NOT_EXIST);
        }
        ossService.deleteFile(resumeDO.getFilePath());
        int rows = resumeMapper.deleteById(id);
        log.info("删除简历成功, id:{}", id);
        return rows > 0;
    }

    @Override
    public JsonData getPreviewUrl(Long id) {
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        //查询简历
        ResumeDO resumeDO = resumeMapper.selectOne(
                new LambdaQueryWrapper<ResumeDO>()
                        .eq(ResumeDO::getAccountId, accountDTO.getId())
                        .eq(ResumeDO::getId,id)
        );
        if(resumeDO == null){
            return JsonData.buildError("简历不存在");
        }else {
            String generatePreviewUrl = ossService.generatePreviewUrl(resumeDO.getFilePath());
            return JsonData.buildSuccess(generatePreviewUrl);
        }
    }

    @Override
    public JsonData analyse(Long id) {
        //查找简历
        //更新简历状态
        //扣减权益
        //发送MQ消息

        return null;
    }

    /**
     * 从MultipartFile中提取文本内容
     * 直接使用文件的InputStream，避免上传后再下载的冗余操作
     */
    private String extractTextFromMultipartFile(MultipartFile file, String fileType) throws IOException {
        log.debug("开始从上传文件提取内容，文件：{}，类型：{}", file.getOriginalFilename(), fileType);

        try (InputStream inputStream = file.getInputStream()) {
            return switch (fileType.toLowerCase()) {
                case "pdf" -> {
                    try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
                        if (document.isEncrypted()) {
                            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
                        }
                        PDFTextStripper stripper = new PDFTextStripper();
                        yield stripper.getText(document);
                    }
                }
                case "doc" -> {
                    try (HWPFDocument document = new HWPFDocument(inputStream);
                         WordExtractor extractor = new WordExtractor(document)) {
                        yield extractor.getText();
                    }
                }
                case "docx" -> {
                    try (XWPFDocument document = new XWPFDocument(inputStream);
                         XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                        yield extractor.getText();
                    }
                }
                default -> throw new BizException(BizCodeEnum.SYSTEM_ERROR);
            };
        } catch (IOException e) {
            log.error("提取文件内容失败：{}", e.getMessage(), e);
            throw e;
        }
    }

    private ResumeDTO convertToDTO(ResumeDO resumeDO) {
        return SpringBeanUtil.copyProperties(resumeDO, ResumeDTO.class);
    }

}
