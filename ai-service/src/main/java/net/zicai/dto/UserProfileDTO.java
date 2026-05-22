package net.zicai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户求职画像DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {

    @JsonProperty("expectedSalary")
    private String expectedSalary;

    @JsonProperty("workYears")
    private String workYears;

    @JsonProperty("targetCity")
    private String targetCity;

    @JsonProperty("targetPosition")
    private String targetPosition;

    @JsonProperty("interviewType")
    private String interviewType;

    @JsonProperty("specialContent")
    private String specialContent;

    @JsonProperty("resumeId")
    private Long resumeId;
}
