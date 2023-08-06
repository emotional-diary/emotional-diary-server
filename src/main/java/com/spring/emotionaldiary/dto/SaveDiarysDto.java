package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.AIComments;
import com.spring.emotionaldiary.model.Diarys;
import com.spring.emotionaldiary.model.Emotion;
import com.spring.emotionaldiary.model.Users;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;
import java.util.Optional;

@Data
public class SaveDiarysDto {

    private String content;

    private Emotion emotion;

    private String metaData;

    private Users users;

    private AIComments aiComments;

    private Timestamp diaryAt;

    public SaveDiarysDto(String content,Emotion emotion, String metaData, Optional<Users> user, AIComments saveAIComments, Timestamp diaryAt) {
        this.content = content;
        this.emotion = emotion;
        this.users = user.get();
        this.metaData = metaData;
        this.aiComments = saveAIComments;
        this.diaryAt =diaryAt;
    }

    public Diarys toDiarys(){
        return Diarys.builder()
                .content(content)
                .diaryAt(diaryAt)
                .emotion(emotion)
                .aiComments(aiComments)
                .users(users)
                .metaData(metaData)
                .build();
    }
}
