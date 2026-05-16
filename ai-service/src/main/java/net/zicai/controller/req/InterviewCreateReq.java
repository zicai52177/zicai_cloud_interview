package net.zicai.controller.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterviewCreateReq {

    /**
     * 期望薪资
     */
    private String expectedSalary;

    /**
     * 工作年限
     */
    private String workYears;

    /**
     * 目标城市
     */
    private String targetCity;

    /**
     * 目标岗位
     */
    private String targetPosition;

    /**
     * 面试类型：SPECIAL_INTERVIEW、COMMON_INTERVIEW
     */
    private String interviewType;

    /**
     * 专项面内容
     */
    private String specialContent = "";

    /**
     * 简历ID
     */
    private Long resumeId;
}
