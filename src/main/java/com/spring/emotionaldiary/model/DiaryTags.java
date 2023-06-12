package com.spring.emotionaldiary.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "diary_tags")
public class DiaryTags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 시퀀스
    @Column(name = "dairy_tag_id")
    private Long diaryTagID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id",nullable = false)
    private Diarys diarys;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id",nullable = false)
    private Tags tags;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updatedAt;
}
