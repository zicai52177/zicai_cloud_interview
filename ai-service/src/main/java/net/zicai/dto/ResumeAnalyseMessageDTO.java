package net.zicai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author wangdi
 * @date 2026/5/3 17:24
 * @description
 */
@Component
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResumeAnalyseMessageDTO {

    private Long resumeId;
    private Long accountId;
    private String messageId;

}
