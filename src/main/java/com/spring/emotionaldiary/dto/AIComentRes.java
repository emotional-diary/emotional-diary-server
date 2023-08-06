package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.AIComments;
import com.spring.emotionaldiary.model.Users;
import lombok.Data;

@Data
public class AIComentRes {
    private AIResponseData data;
    private String responseMessage;
    private int statusCode;

    public AIComments toAIComments(){
        return AIComments.builder()
                .comment(data.getAnswer())
                .build();
    }
}
