package net.zicai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.ResumeDTO;
import net.zicai.service.ResumeService;
import net.zicai.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author wangdi
 * @date 2026/5/1 09:29
 * @description
 */
@RestController
@RequestMapping("/api/v1/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @RequestMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传简历并解析")
    public JsonData upload(
            @Parameter(name = "file")
            @RequestPart MultipartFile file) {

        ResumeDTO resumeDTO = resumeService.upLodeAndParseFile(file);


        return JsonData.buildSuccess(resumeDTO);
    }

    @GetMapping("/list")
    @Operation(summary = "查询当前用户简历列表")
    public JsonData list() {
        List<ResumeDTO> resumeDTOList = resumeService.list();
        return JsonData.buildSuccess(resumeDTOList);
    }

    @GetMapping("/detail")
    @Operation(summary = "查询简历详情")
    public JsonData detail(@RequestParam("id") Long id) {
        ResumeDTO resumeDTO = resumeService.findById(id);
        return JsonData.buildSuccess(resumeDTO);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除简历")
    public JsonData delete(@RequestParam("id") Long id) {
        boolean result = resumeService.deleteById(id);
        return JsonData.buildSuccess(result);
    }

    @GetMapping("/preview")
    @Operation(summary = "预览简历")
    public JsonData preview(@RequestParam("id") Long id) {
        return resumeService.getPreviewUrl(id);
    }

    @GetMapping("/analyse")
    @Operation(summary = "简历解析")
    public JsonData analyse(@RequestParam("id") Long id) {
        return resumeService.analyse(id);
    }

}
