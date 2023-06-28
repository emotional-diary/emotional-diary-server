package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.Diarys;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

@Getter
public class DiarysDto {
    private Long diaryID;

    private String content;

    private String imageUrl;

    private String emotion;

    private String metaData;

    private Long userID;
    private String userName;
    private Long commentID;
    private String comment;
    private Timestamp diaryAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public DiarysDto(Diarys d) {
        diaryID = d.getDiaryID();
        content = d.getContent();
        imageUrl = d.getImageUrl();
        emotion = d.getEmotion();
        metaData = d.getMetaData();
        userID = d.getUsers().getUserID();
        userName = d.getUsers().getName();
        commentID = d.getAiComments().getCommentID();
        comment = d.getAiComments().getComment();
        // DateTimeFormatter를 사용하여 Timestamp 필드를 원하는 형식으로 변환합니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");
        diaryAtFormatted = d.getDiaryAt().toLocalDateTime().format(formatter);
        createdAtFormatted = d.getCreatedAt().toLocalDateTime().format(formatter);
        updatedAtFormatted = d.getUpdatedAt().toLocalDateTime().format(formatter);
    }

    // Timestamp 필드를 문자열로 변환한 값
    private String diaryAtFormatted;
    private String createdAtFormatted;
    private String updatedAtFormatted;
}
