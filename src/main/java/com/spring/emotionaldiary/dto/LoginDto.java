package com.spring.emotionaldiary.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class  LoginDto {

    @NotBlank(message = "이메일을 입력해주세요")
    @Length(min = 6,max = 254,message = "이메일의 길이는 6~254자압니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
//    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,128}$", message = "최소 하나의 문자 및 숫자,특수문자를 포함한 8~128자이여야 합니다")
    private String password;
}
