package net.xdclass.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.controller.req.AnswerReq;
import net.xdclass.controller.req.InterviewCreateReq;
import net.xdclass.controller.req.InterviewFinishReq;
import net.xdclass.controller.req.InterviewPageReq;
import net.xdclass.dto.AccountDTO;
import net.xdclass.interceptor.AccountLoginInterceptor;
import net.xdclass.service.InterviewService;
import net.xdclass.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI面试控制器
 * <p>提供面试创建、答题、结束、查询、删除等接口</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/interview")
@Tag(name = "面试管理", description = "AI面试相关接口")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    /**
     * 创建面试
     */
    @PostMapping("create")
    @Operation(summary = "创建面试", description = "创建AI面试，扣减权益后异步生成面试内容")
    public JsonData create(@RequestBody InterviewCreateReq req) {
        return interviewService.create(req);
    }

    /**
     * 查询面试状态
     */
    @GetMapping("status")
    @Operation(summary = "查询面试状态", description = "查询指定面试的生成状态和进度信息")
    public JsonData getInterviewStatus(
            @Parameter(description = "面试ID", required = true)
            @RequestParam("interviewId") Long interviewId) {
        return interviewService.getInterviewStatus(interviewId);
    }

    /**
     * 回答题目
     */
    @PostMapping("answer")
    @Operation(summary = "回答题目", description = "提交答案，异步进行AI评估")
    public JsonData answer(@RequestBody AnswerReq req) {
        return interviewService.answer(req);
    }

    /**
     * 结束面试（异步处理）
     */
    @PostMapping("finish")
    @Operation(summary = "结束面试", description = "结束面试并异步生成综合评价报告")
    public JsonData finish(@RequestBody InterviewFinishReq req) {
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        return interviewService.finishAsync(req, accountDTO.getId());
    }

    /**
     * 查看面试详情
     */
    @GetMapping("view")
    @Operation(summary = "查看面试详情", description = "返回本次面试的全部轮次和轮次里面的全部面试题")
    public JsonData view(
            @Parameter(description = "面试ID", required = true)
            @RequestParam("interviewId") Long interviewId) {
        return interviewService.viewInterviewDetail(interviewId);
    }

    /**
     * 分页查询面试记录
     */
    @PostMapping("page")
    @Operation(summary = "分页查询面试记录", description = "分页查询当前用户的面试记录")
    public JsonData page(@RequestBody InterviewPageReq req) {
        return interviewService.page(req);
    }

    /**
     * 删除面试记录
     */
    @DeleteMapping("delete")
    @Operation(summary = "删除面试记录", description = "根据面试ID删除面试记录，同时删除相关的轮次和题目")
    public JsonData delete(
            @Parameter(description = "面试记录ID", required = true)
            @RequestParam("interviewId") Long interviewId) {
        return interviewService.delete(interviewId);
    }
}
