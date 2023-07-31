package com.spring.emotionaldiary.controller;

import com.spring.emotionaldiary.dto.ModelInfo;
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

@RestController
@Slf4j
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    @PostMapping("/predict")
    public @ResponseBody ResponseEntity<String> predict(@RequestBody ModelInfo modelInfo) {
        HttpHeaders headers = new HttpHeaders();
        // Add any required headers, such as Content-Type if needed.
        // headers.add("Content-Type", "application/json");

        HttpEntity<ModelInfo> requestEntity = new HttpEntity<>(modelInfo, headers);
        RestTemplate rt = new RestTemplate();

        ResponseEntity<String> response = rt.exchange(
                "http://192.168.25.11:5000/api/model/predict",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        return response;
    }

//    @GetMapping("/data")
//    public String getData() {
//        String inputText = "안녕";
//        // AI 서버의 주소
//        String aiServerUrl = "http://192.168.25.11:5000/api/model/predict";
//
//        // HTTP 요청을 보낼 때 사용할 RestTemplate 객체 생성
//        RestTemplate restTemplate = new RestTemplate();
//
//        // 요청의 헤더 설정
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
//
//        // AI 서버로 POST 요청 보내기
//        String response = restTemplate.postForObject(aiServerUrl, request, String.class);
//
//        // AI 서버로부터 받은 응답 처리 (예를 들면 답변 데이터 추출)
//        // String answer = parseResponse(response);
//
//        return response;
//    }

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