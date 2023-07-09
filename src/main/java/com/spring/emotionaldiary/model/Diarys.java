package com.spring.emotionaldiary.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Diarys {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 시퀀스
    @Column(name = "diary_id")
    private Long diaryID;

    @Column(nullable = false)
    private String title; // ㅈㅔ목 없애기

    @Column(columnDefinition = "TEXT",nullable = false)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private String emotion;

    @Column(columnDefinition = "TEXT",name = "meta_data")
    private String metaData;

    // fetch 종류 : EAGER, LAZY
    // LAZY -> JSON 에러 남, hibernateLazyInitializer를 직렬화 에러
    // 직렬화 :Object를 연속된 String 데이터나 연속된 Bytes 데이터로 바꾸는 것
    // EAGER -> 에러는 안나지만, users의 모든 데이터 조회됨
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "writer_id",nullable = false)
    private Users users;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comment_id",nullable = false)
    private AIComments aiComments;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    @Column(name = "diary_at",nullable = false)
    private Timestamp diaryAt;
}
