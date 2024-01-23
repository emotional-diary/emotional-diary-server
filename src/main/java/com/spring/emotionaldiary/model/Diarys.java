package com.spring.emotionaldiary.model;

import com.spring.emotionaldiary.dto.ImgRes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Diarys {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 시퀀스
    @Column(name = "diary_id")
    private Long diaryID;

    @Column(columnDefinition = "TEXT",nullable = false)
    @NotBlank(message = "일기 내용을 입력해주세요")
    private String content;

    // JPA의 Transient 애노테이션 : 엔티티 객체의 데이터와 테이블의 컬럼과 매핑하고 있는 관계 제외
    // 해당 데이터를 테이블의 컬럼과 매핑X
    @Transient
    private final List<ImgRes> images = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Emotion emotion;

    @Column(columnDefinition = "TEXT",name = "meta_datㄴ")
    private String metaData;

    // fetch 종류 : EAGER, LAZY
    // LAZY -> JSON 에러 남, hibernateLazyInitializer를 직렬화 에러
    // 직렬화 :Object를 연속된 String 데이터나 연속된 Bytes 데이터로 바꾸는 것
    // EAGER -> 에러는 안나지만, users의 모든 데이터 조회됨
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "writer_id",nullable = false)
    private Users users;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "comment_id",nullable = false)
    private AIComments aiComments;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    @Column(name = "diary_at",nullable = false)
    private LocalDate diaryAt;
}
