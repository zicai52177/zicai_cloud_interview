package net.zicai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author 王镝
 * @date 2026 04 16
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsDTO {

    private String mobile;

    private String templateId;

    private String smsSignId;

    private String param;


}
