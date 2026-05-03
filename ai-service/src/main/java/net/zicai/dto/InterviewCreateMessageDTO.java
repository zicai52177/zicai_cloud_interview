package net.zicai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author wangdi
 * @date 2026/5/3 17:18
 * @description
 */
@Component
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterviewCreateMessageDTO {

    private Long interviewId;

    private Long accountId;

    private String messageId;
}
