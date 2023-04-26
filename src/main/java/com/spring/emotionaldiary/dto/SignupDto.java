package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor //final이 붙거나 @NotNull이 붙은 필드의 생성자를 자동 생성해주는 롬복 어노테이션
public class SignupDto {

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z]).{8,16}", message = "최소 하나의 문자 및 숫자를 포함한 8~16자이여야 합니다")
    private String pwd;

    @NotBlank(message = "이름을 입력해주세요")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z]{2,16}$", message = "숫자 또는 특수문자를 제외한 2자이상 입력해주세요")
    private String name;

    private String birth;
    private GenderType gender;
    private LoginType loginType;
    private Timestamp created_at;
    private Timestamp updated_at;

    private List<Long> termIds;
    private boolean isAgree;

    @Builder
    public SignupDto(String email, String pwd, String name, String birth, GenderType gender) {
        this.email = email;
        this.pwd = pwd;
        this.name = name;
        this.birth = birth;
        this.gender = gender;
    }

    public Users toUser(){
        return Users.builder()
                .email(email)
                .pwd(pwd)
                .name(name)
                .birth(birth)
                .gender(gender)
                .login_type(LoginType.LOCAL)
                .build();
    }

    // toUserTerms 메서드에서는 List<UserTerms>를 반환하도록 수정하고, termIds를 반복문으로 돌면서
    // 각각의 약관에 대한 UserTerms 객체를 생성하고, 생성된 UserTerms객체를 userTermsList에 추가하도록 수정
    // 이렇게 하면 SignupDto 객체를 통해 여러 개의 약관에 대한 UserTerms 객체를 생성할 수 있음
//    public List<UserTerms> toUserTerms(){
//        List<UserTerms> userTermsList = new ArrayList<>();
//        for (Long termId : termIds) {
//            UserTerms userTerms = UserTerms.builder()
//                    .users(toUser())
//                    .terms(Terms.builder().term_id(termId).build())
//                    .isAgree(isAgree)
//                    .build();
//            userTermsList.add(userTerms);
//        }
//        return userTermsList;
//    }
}
