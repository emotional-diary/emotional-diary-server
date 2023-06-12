package com.spring.emotionaldiary.dto;

import lombok.Data;

@Data
public class PasswordDto {
    private String email;
    private String password;
    private String newPassword;
}
