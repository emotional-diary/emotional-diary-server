package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.LoginType;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;

@Data
@RequiredArgsConstructor
public class SignupRes {
    private LoginType loginType;
}
