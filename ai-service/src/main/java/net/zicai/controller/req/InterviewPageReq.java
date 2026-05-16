package net.xdclass.controller.req;

import lombok.Data;

@Data
public class InterviewPageReq {

    /**
     * 当前页码，默认第1页
     */
    private int page = 1;

    /**
     * 每页大小，默认10条
     */
    private int size = 10;
}
