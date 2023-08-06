package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.Emotion;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import java.sql.Timestamp;

@Data
@RequiredArgsConstructor //final이 붙거나 @NotNull이 붙은 필드의 생성자를 자동 생성해주는 롬복 어노테이션
public class DiaryDto {

    @NotBlank(message = "일기 내용을 입력해주세요")
    private String content;

    @PastOrPresent(message = "과거와 현재의 일기만 작성이 가능합니다")
    private Timestamp diaryAt;

    private Emotion emotion;

    private String metaData;
}
