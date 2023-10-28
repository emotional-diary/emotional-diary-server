package com.spring.emotionaldiary.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@NoArgsConstructor
@Entity(name = "diary_imgs")
public class DiaryImgs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 시퀀스
    @Column(name = "diary_img_id")
    private Long diaryImgID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // diary가 삭제될때 img 같이 삭제
    private Diarys diarys;

    @Column(nullable = false)
    private String imgUrl;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    public DiaryImgs(String imgUrl, Diarys diarys) {
        this.imgUrl = imgUrl;
        this.diarys = diarys;
    }
}
