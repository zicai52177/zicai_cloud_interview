package net.zicai.controller.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerReq {

    /**
     * 用户回答内容
     */
    private String answer;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 面试ID
     */
    private Long interviewId;
}
