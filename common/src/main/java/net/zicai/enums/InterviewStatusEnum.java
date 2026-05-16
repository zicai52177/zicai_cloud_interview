package net.xdclass.enums;

/**
 * 面试状态枚举
 */
public enum InterviewStatusEnum {

    GENERATING("GENERATING", "生成中,正在解析简历..."),
    GENERATE_ROUND("GENERATE_ROUND", "正在生成面试轮次..."),
    GENERATE_QA("GENERATE_QA", "正在生成题目..."),
    IN_PROGRESS("IN_PROGRESS", "面试生成完成，可以开始答题"),
    EVALUATING("EVALUATING", "评估中"),
    COMPLETED("COMPLETED", "已完成评估"),
    CANCELLED("CANCELLED", "已取消"),
    FAILED_PARSE_RESUME("FAILED_PARSE_RESUME", "简历解析失败，请重新上传"),
    FAILED_GENERATE_ROUND("FAILED_GENERATE_ROUND", "生成面试轮次失败"),
    FAILED_GENERATE_QA("FAILED_GENERATE_QA", "生成面试题目失败"),
    FAILED_CREATE_INTERVIEW("FAILED_CREATE_INTERVIEW", "出异常了,生成面试失败"),
    FAILED_EVALUATE_INTERVIEW("FAILED_EVALUATE_INTERVIEW", "出异常了,评估面试情况失败");

    private final String code;
    private final String desc;

    InterviewStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static InterviewStatusEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (InterviewStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
