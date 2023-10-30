package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.Emotion;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class updateDiaryDto {
    private String content;
    private Emotion emotion;
    private LocalDate diaryAt;
    private String metaData;
    private List<String> images;
    private List<Long> deleteImgIDList;
}
