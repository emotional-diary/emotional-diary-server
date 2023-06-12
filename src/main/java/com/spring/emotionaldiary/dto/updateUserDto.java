package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.GenderType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class updateUserDto {

    @NotBlank(message = "이름을 입력해주세요")
    private String name;

    private String birth;

    private GenderType gender;
}
