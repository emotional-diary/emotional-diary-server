package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.DiaryImgs;
import com.spring.emotionaldiary.model.Diarys;
import com.spring.emotionaldiary.model.Emotion;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DiarysDto {
    private Long diaryID;

    private String content;

    private Emotion emotion;

    private String metaData;

    private List<ImgRes> imgsList;

    private Long userID;
    private String userName;
    private Long commentID;
    private String comment;
    private LocalDate diaryAt;

    public DiarysDto(Diarys d) {
        diaryID = d.getDiaryID();
        content = d.getContent();
        emotion = d.getEmotion();
        metaData = d.getMetaData();
        imgsList = d.getImgsList();
        userID = d.getUsers().getUserID();
        userName = d.getUsers().getName();
        commentID = d.getAiComments().getCommentID();
        comment = d.getAiComments().getComment();
        diaryAt = d.getDiaryAt();
    }
}
