package com.spring.emotionaldiary.model;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.sql.Timestamp;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity //해당 클래스를 테이블로 인식할 수 있도록 만드는 어노테이션
public class Users {
    @Id // 해당 변수가 primary key로 사용할 수 있는 식별자의 역할을 할 수 있도록 하는 어노테이션
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 시퀀스
    @Column(name = "user_id")
    private Long userId;

    @NotBlank(message = "이메일을 입력해주세요")
    @Length(min = 6,max = 254,message = "이메일의 길이는 6~254자압니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    @Column(nullable = false, unique = true,length = 254)
    private String email;

    @Column(length = 128)
    private String password;

    @NotBlank(message = "이름을 입력해주세요")
    @Pattern(regexp = "^[가-힣a-z0-9]{2,16}$", message = "숫자, 특수문자, 이모지, 공백, 자음, 모음을 제외한 2~16자를 입력해주세요")
    @Column(nullable = false, length = 16)
    private String name;

    @Length(min = 6,max = 6,message = "생년월일 6자리를 입력해주세요")
    private String birth;

    @Enumerated(EnumType.STRING)
    private GenderType gender;

    @Column(name = "login_type",nullable = false)
    @ColumnDefault("'LOCAL'")
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    public Users(String email, String password, String name, String birth, GenderType gender, LoginType loginType) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birth = birth;
        this.gender = gender;
        this.loginType = loginType;
    }
}