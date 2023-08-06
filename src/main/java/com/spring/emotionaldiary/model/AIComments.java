package com.spring.emotionaldiary.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.xml.sax.ext.LexicalHandler;

import javax.persistence.*;
import java.sql.Timestamp;

@Builder
@Data
@Entity(name = "ai_comments")
@AllArgsConstructor
@NoArgsConstructor
public class AIComments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 시퀀스
    @Column(name = "comment_id")
    private Long commentID;

    @Column(columnDefinition = "TEXT",nullable = false)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

}
