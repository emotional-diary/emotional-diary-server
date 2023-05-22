package com.spring.emotionaldiary.dto;

import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;

@Data
public class TermsDto {
    @NotBlank(message = "약관 아이디가 필요합니다")
    private Long termId;

    @NotBlank(message = "동의 여부가 필요합니다")
    private String isAgree;
}
