package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@RequiredArgsConstructor //final이 붙거나 @NotNull이 붙은 필드의 생성자를 자동 생성해주는 롬복 어노테이션
public class SignupDto {

    @NotBlank(message = "이메일을 입력해주세요")
    @Length(min = 6,max = 254,message = "이메일의 길이는 6~254자압니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    private String password;

    @NotBlank(message = "이름을 입력해주세요")
    @Pattern(regexp = "^[가-힣a-z0-9]{2,16}$", message = "숫자, 특수문자, 이모지, 공백, 자음, 모음을 제외한 2~16자를 입력해주세요")
    private String name;

    @Length(min = 6,max = 6,message = "생년월일 6자리를 입력해주세요")
    private String birth;

    private GenderType gender;

    @ColumnDefault("'LOCAL'")
    private LoginType loginType;

    private List<TermsDto> terms;

    @Builder
    public SignupDto(String email, String password, String name, String birth, GenderType gender,LoginType loginType,List<TermsDto> terms) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birth = birth;
        this.gender = gender;
        this.loginType = loginType;
        this.terms = terms;
    }

    public Users toUser(){
        return Users.builder()
                .email(email)
                .password(password)
                .name(name)
                .birth(birth)
                .gender(gender)
                .loginType(loginType)
                .build();
    }
}
