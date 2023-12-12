package com.spring.emotionaldiary.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@RequiredArgsConstructor
public class InquiryDto {
    @NotBlank(message = "이메일을 입력해주세요")
    @Length(min = 6,max = 254,message = "이메일의 길이는 6~254자압니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @NotBlank(message = "문의 내용을 입력해주세요")
    private String content;

    @NotBlank(message = "웹 및 기기 정보를 입력해주세요")
    private String userAgent; // 웹 및 기기 정보

    public InquiryDto(String email, String content, String userAgent) {
        this.email = email;
        this.content = content;
        this.userAgent = userAgent;
    }
}
