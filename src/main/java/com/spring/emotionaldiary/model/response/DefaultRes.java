package com.spring.emotionaldiary.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor //자동 생성자 생성
@Builder
public class DefaultRes<T> {
    private int statusCode;
    private String responseMessage;
    private T data;

    //데이터가 없는경우 넘겨줄 response
    public DefaultRes(final int statusCode,final String responseMessage) {
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
        this.data = null;
    }

    public static<T> DefaultRes<T> res(final int statusCode, final String responseMessage) {
        return res(statusCode, responseMessage, null);
    }

    //builder 사용
    public static<T> DefaultRes<T> res(final int statusCode, final String responseMessage, final T t) {
        return DefaultRes.<T>builder()
                .data(t)
                .statusCode(statusCode)
                .responseMessage(responseMessage)
                .build();
    }
}
