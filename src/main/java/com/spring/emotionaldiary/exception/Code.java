package com.spring.emotionaldiary.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum Code {
    WRONG_INPUT_IMAGE(HttpStatus.BAD_REQUEST, "204", "이미지는 반드시 있어야 합니다"),
    IMAGE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "210", "이미지 업로드에 실패했습니다"),
    WRONG_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "211", "지원하지 않는 파일 형식입니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String msg;

    Code(HttpStatus httpStatus, String code, String msg) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.msg = msg;
    }
}
