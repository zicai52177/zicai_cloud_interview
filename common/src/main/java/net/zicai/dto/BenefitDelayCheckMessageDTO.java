package net.zicai.dto;

/**
 * @author wangdi
 * @date 2026/5/2 13:54
<<<<<<< HEAD
 * @description
 */

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 权益延迟检查MQ消息DTO
 * 用于account-service发送延迟检查消息，检查业务是否完成
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BenefitDelayCheckMessageDTO", description = "权益延迟检查MQ消息")
public class BenefitDelayCheckMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID（任务表主键）")
    private String messageId;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "检查级别：1-第1次检查(1分钟)，2-第2次检查(5分钟)")
    private Integer checkLevel;
}
=======
 * @description 
 */
public class BenefitDelayCheckMessageDTO {
}
>>>>>>> b7821a595bc942521dcd7b498f008f953d501850
