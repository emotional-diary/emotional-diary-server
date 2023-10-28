package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.Emotion;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Data
@RequiredArgsConstructor //final이 붙거나 @NotNull이 붙은 필드의 생성자를 자동 생성해주는 롬복 어노테이션
public class DiaryDto {

    @NotBlank(message = "일기 내용을 입력해주세요")
    private String content;

    private LocalDate diaryAt;

    private Emotion emotion;

    private String metaData;
}
