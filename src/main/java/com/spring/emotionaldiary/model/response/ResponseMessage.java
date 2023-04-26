package com.spring.emotionaldiary.model.response;

public class ResponseMessage {
    public static final String CREATED_USER = "회원 가입 성공";
    public static final String DUPLICATE_EMAIL = "이메일 중복 에러";
    public static final String VALIDATION_ERROR = "이메일 유효성 에러";
    public static final String SEND_EMAIL_AUTHENTICATION_CODE = "이메일 인증코드 전송 성공";
    public static final String LOGIN_SUCCESS = "로그인 성공";
    public static final String LOGIN_FAIL = "로그인 실패";
    // public static final String READ_USER = "회원 정보 조회 성공";
    // public static final String NOT_FOUND_USER = "회원을 찾을 수 없습니다.";

    // public static final String UPDATE_USER = "회원 정보 수정 성공";
    // public static final String DELETE_USER = "회원 탈퇴 성공";
    public static final String INTERNAL_SERVER_ERROR = "서버 내부 에러";
    // public static final String DB_ERROR = "데이터베이스 에러";
}
