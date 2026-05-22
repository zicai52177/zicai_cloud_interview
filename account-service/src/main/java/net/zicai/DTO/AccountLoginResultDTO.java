package net.zicai.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.zicai.dto.AccountDTO;

/**
 * @author 王镝
 * @date 2026 04 21
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountLoginResultDTO {

    private Boolean success;

    private String token;

    private AccountDTO accountDTO;

    private String errorMessage;

    private String qrCodeStatus;

    private String sceneId;
}
