package com.spring.emotionaldiary.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@RequiredArgsConstructor
public class EmailAuthenticationDto {

    @NotBlank(message = "이메일을 입력해주세요")
    @Length(min = 6,max = 254,message = "이메일의 길이는 6~254자압니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    private String code;
}
