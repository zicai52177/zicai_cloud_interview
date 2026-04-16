package net.zicai.enums;

import lombok.Getter;

@Getter
public enum CheckCodeTypeEnum {
    /**
     * 登录
     */
    LOGIN,
    /**
     * 绑定
     */
    BIND,
    /**
     * 修改密码
     */
    ChangePassword,
}