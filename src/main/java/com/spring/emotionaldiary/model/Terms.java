package com.spring.emotionaldiary.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 시퀀스
    private Long term_id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean is_required;

    @CreationTimestamp
    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private Timestamp updated_at;
}