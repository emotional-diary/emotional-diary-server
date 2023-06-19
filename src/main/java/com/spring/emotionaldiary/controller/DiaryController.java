package com.spring.emotionaldiary.controller;

import com.spring.emotionaldiary.dto.DiarysRes;
import com.spring.emotionaldiary.model.Diarys;
import com.spring.emotionaldiary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping("/data")
    public void getData() {
        // 데이터 처리 로직
        RestTemplate restTemplate = new RestTemplate();

        // Flask 서버의 URL
        String flaskUrl = "http://127.0.0.1:5000";

// HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

// 보낼 데이터 준비
        String content = "This is a comment to be analyzed";

// HTTP 요청 엔티티 생성
        HttpEntity<String> requestEntity = new HttpEntity<>(content, headers);

// Flask 서버로 POST 요청 전송
        ResponseEntity<String> responseEntity = restTemplate.exchange(flaskUrl, HttpMethod.POST, requestEntity, String.class);

// 응답 받은 comment 출력
        String analyzedComment = responseEntity.getBody();
        System.out.println("Analyzed Comment: " + analyzedComment);
    }

    @GetMapping("/api/v1/users/diary")
    public List<Diarys> getDiarys(@RequestParam String startAt, @RequestParam String endAt) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date startDate = dateFormat.parse(startAt);
            Date endDate = dateFormat.parse(endAt);
            return diaryService.getDiarysByDiaryAt(startDate, endDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}