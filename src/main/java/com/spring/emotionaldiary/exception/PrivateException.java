package com.spring.emotionaldiary.exception;

import lombok.Getter;

@Getter
public class PrivateException extends RuntimeException{
    private Code code;

    public PrivateException(Code code){
        super(code.getMsg());
        this.code = code;
    }
}
