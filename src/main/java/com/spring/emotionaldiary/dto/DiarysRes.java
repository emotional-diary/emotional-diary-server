package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.AIComments;
import com.spring.emotionaldiary.model.Diarys;
import com.spring.emotionaldiary.model.Users;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;


@Getter
public class DiarysRes {
    private Long diaryID;

    private String title;

    private String content;

    private String imageUrl;

    private String emotion;

    private String metaData;

    private Long userID;
    private String name;
    private Long commentID;
    private String comment;
    private Timestamp diaryAt;

    public DiarysRes(Long diaryID, String title, String content, String imageUrl, String emotion, String metaData, Long userId, String name, Long commentID, String comment, Timestamp diaryAt) {
    }
}
