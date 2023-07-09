package com.spring.emotionaldiary.controller;

import com.spring.emotionaldiary.dto.DiarysDto;
import com.spring.emotionaldiary.dto.updateUserDto;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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

    // 내 일기 조회(조회 조건 : startAt, endAt, page, size, sort 가능)
    @GetMapping("/api/v1/users/diarys")
    public ResponseEntity getDiarys(@RequestParam(required = false) String startAt, @RequestParam(required = false)  String endAt, Pageable diaryPageable, Authentication authentication) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date startDate = null;
            Date endDate = null;
            if (startAt == null && endAt == null) {
                // startAt과 endAt이 모두 null인 경우, 전체 기간으로 설정
                startDate = dateFormat.parse("1900-01-01");
                endDate = new Date();
            } else if (startAt == null) {
                // startAt이 null인 경우, endAt을 기준으로 startDate 설정
                startDate = dateFormat.parse("1900-01-01");
                endDate = dateFormat.parse(endAt);
            } else if (endAt == null) {
                // endAt이 null인 경우, startAt을 기준으로 endDate 설정
                startDate = dateFormat.parse(startAt);
                endDate = new Date();
            } else {
                startDate = dateFormat.parse(startAt);
                endDate = dateFormat.parse(endAt);
            }
            System.out.println(endDate);

            return diaryService.getDiarysByDiaryAtAndUserEmail(startDate, endDate,(String) authentication.getDetails(),diaryPageable);
        } catch (ParseException e) {
            return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, "날짜 형식 에러"), HttpStatus.BAD_REQUEST);
        } catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 일기 삭제
    @DeleteMapping("/api/v1/users/diary/{diaryID}")
    public ResponseEntity getDiarys(@PathVariable("diaryID") Long diaryID) {
        try {
            return diaryService.deleteDiary(diaryID);
        } catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}