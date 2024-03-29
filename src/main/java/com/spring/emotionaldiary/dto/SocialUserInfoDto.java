package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.GenderType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
public class SocialUserInfoDto {
    private String email;
    private String gender;

    public SocialUserInfoDto(String email,String gender) {
        this.email = email;
        this.gender = gender;
    }
//    private String birth;
//    private GenderType gender;
}
