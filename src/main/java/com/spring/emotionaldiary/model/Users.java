package com.spring.emotionaldiary.model;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    private Long user_id;

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z]).{8,16}", message = "최소 하나의 문자 및 숫자를 포함한 8~16자이여야 합니다")
    @Column(nullable = false, length = 128)
    private String pwd;

    @NotBlank(message = "이름을 입력해주세요")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z]{2,16}$", message = "숫자 또는 특수문자를 제외한 2자이상 입력해주세요")
    @Column(nullable = false)
    private String name;

    private String birth;

    @Enumerated(EnumType.STRING)
    private GenderType gender;

    @Column(nullable = false)
    @ColumnDefault("'LOCAL'")
    @Enumerated(EnumType.STRING)
    private LoginType login_type;

    @CreationTimestamp
    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private Timestamp updated_at;
}