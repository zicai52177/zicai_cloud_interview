package net.zicai.enums;

/**
 * 题目状态枚举
 */
public enum QuestionStatusEnum {

    UNANSWERED("UNANSWERED", "未回答"),
    ANSWERED("ANSWERED", "已回答"),
    EVALUATING("EVALUATING", "评估中"),
    COMPLETED("COMPLETED", "已完成");

    private final String code;
    private final String desc;

    QuestionStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static QuestionStatusEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (QuestionStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
