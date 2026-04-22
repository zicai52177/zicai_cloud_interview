package net.zicai.service;

import net.zicai.DTO.AccountLoginResultDTO;
import net.zicai.controller.req.AccountLoginReq;
import net.zicai.controller.req.SendCheckCodeReq;
import net.zicai.dto.AccountDTO;
import net.zicai.util.JsonData;

/**
* @author 王镝
* @date 20260416
**/


public interface AccountService {

    JsonData sendCheckCode(SendCheckCodeReq req);

    AccountLoginResultDTO login(AccountLoginReq req);

    AccountDTO findById();
}
