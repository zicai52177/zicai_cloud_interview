package net.zicai.controller.req;

import lombok.Data;

/**
 * @author 王镝
 * @date 2026 04 21
 **/

@Data
public class AccountLoginReq {

    private String identifier;

    private String checkCode;

    private String type;
}
