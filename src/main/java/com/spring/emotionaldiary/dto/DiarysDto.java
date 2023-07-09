package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.Diarys;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
public class DiarysDto {
    private Long diaryID;

    private String title;

    private String content;

    private String imageUrl;

    private String emotion;

    private String metaData;

    private Long userID;
    private String userName;
    private Long commentID;
    private String comment;
    private Timestamp diaryAt;

    public DiarysDto(Diarys d) {
        diaryID = d.getDiaryID();
        title = d.getTitle();
        content = d.getContent();
        imageUrl = d.getImageUrl();
        emotion = d.getEmotion();
        metaData = d.getMetaData();
        userID = d.getUsers().getUserID();
        userName = d.getUsers().getName();
        commentID = d.getAiComments().getCommentID();
        comment = d.getAiComments().getComment();
        diaryAt = d.getDiaryAt();
    }
}
