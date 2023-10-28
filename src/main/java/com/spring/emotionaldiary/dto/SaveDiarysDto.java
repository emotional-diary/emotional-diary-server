package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.AIComments;
import com.spring.emotionaldiary.model.Diarys;
import com.spring.emotionaldiary.model.Emotion;
import com.spring.emotionaldiary.model.Users;
import lombok.Data;

import javax.validation.constraints.PastOrPresent;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;

@Data
public class SaveDiarysDto {

    private String content;

    private Emotion emotion;

    private String metaData;

    private Users users;

    private AIComments aiComments;

    private LocalDate diaryAt;

    public SaveDiarysDto(String content, Emotion emotion, String metaData, Optional<Users> user, AIComments saveAIComments, @PastOrPresent(message = "과거와 현재의 일기만 작성이 가능합니다") LocalDate diaryAt) {
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
