package com.spring.emotionaldiary.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 시퀀스
    @Column(name = "tag_id")
    private Long tagID;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Emotion emotion;

    @Column(nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updatedAt;
}
