package net.xdclass.enums;

/**
 * 轮次状态枚举
 */
public enum RoundStatusEnum {
    
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    EVALUATING("EVALUATING", "评估中"),
    COMPLETED("COMPLETED", "已完成"),
    FAILED("FAILED", "失败");
    
    private final String code;
    private final String desc;
    
    RoundStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    /**
     * 根据code获取对应的枚举
     * @param code 状态码
     * @return 对应的枚举，未找到返回null
     */
    public static RoundStatusEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (RoundStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
