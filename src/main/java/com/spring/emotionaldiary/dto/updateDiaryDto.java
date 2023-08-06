package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.Emotion;
import lombok.Data;

import javax.validation.constraints.PastOrPresent;
import java.sql.Timestamp;

@Data
public class updateDiaryDto {
    private String content;
    private Emotion emotion;
    @PastOrPresent(message = "과거와 현재의 일기만 작성이 가능합니다")
    private Timestamp diaryAt;
    private String metaData;
}
