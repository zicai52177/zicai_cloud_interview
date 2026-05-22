package net.zicai.controller;

import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.BannerDTO;
import net.zicai.service.BannerService;
import net.zicai.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Banner控制器
 */
@RestController
@RequestMapping("/api/v1/banner")
@Slf4j
public class BannerController {

    @Autowired
    private BannerService bannerService;

    /**
     * 查询Banner详情
     *
     * @param id Banner ID
     * @return Banner详情
     */
    @GetMapping("/detail")
    public JsonData detail(@RequestParam("id") Long id) {
        log.info("查询Banner详情请求, id:{}", id);
        BannerDTO bannerDTO = bannerService.findById(id);
        return JsonData.buildSuccess(bannerDTO);
    }

    /**
     * 查询Banner列表
     *
     * @param location 位置（可选）
     * @param status   状态（可选，ON在线/OFF下线）
     * @return Banner列表
     */
    @GetMapping("/list")
    public JsonData list(
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "status", required = false) String status) {
        log.info("查询Banner列表请求, location:{}, status:{}", location, status);
        List<BannerDTO> bannerList = bannerService.list(location, status);
        return JsonData.buildSuccess(bannerList);
    }
}
