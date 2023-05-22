package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.GenderType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
public class SocialUserInfoDto {
    private String email;
    private String name;

    public SocialUserInfoDto(String email, String name) {
        this.email = email;
        this.name = name;
    }
//    private String birth;
//    private GenderType gender;
}
