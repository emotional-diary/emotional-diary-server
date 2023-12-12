package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.Emotion;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Data
@RequiredArgsConstructor //final이 붙거나 @NotNull이 붙은 필드의 생성자를 자동 생성해주는 롬복 어노테이션
public class DiaryDto {

    @NotBlank(message = "일기 내용을 입력해주세요")
    private String content;

    @NotNull(message = "일기의 날짜를 입력해주세요")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) //2000-12-10
    private LocalDate diaryAt;

    @NotNull(message = "감정을 선택해 주세요")
    private Emotion emotion;

    private String metaData;

    private List<String> images;
}
