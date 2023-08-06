package com.spring.emotionaldiary.controller;

import com.spring.emotionaldiary.dto.DiaryDto;
import com.spring.emotionaldiary.dto.DiarysDto;
import com.spring.emotionaldiary.dto.updateDiaryDto;
import com.spring.emotionaldiary.dto.updateUserDto;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.service.DiaryService;
import com.spring.emotionaldiary.utils.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class DiaryController {
    @Autowired
    private DiaryService diaryService;
    @Autowired
    private ValidateUtil validateUtil;

    // 내 일기 작성
    @PostMapping("/diary")
    public ResponseEntity createDiary(@Valid @RequestBody DiaryDto diaryDto, Errors errors, Authentication authentication){
        try {
            if(errors.hasErrors()){
                /* 유효성 통과 못한 필드와 메시지를 핸들링 */
                Map<String, String> validatorResult = validateUtil.validateHandling(errors);
                System.out.println(validatorResult);
                for (String key : validatorResult.keySet()) {
                    System.out.println(key);
                    System.out.println(validatorResult.get(key));
                    return new ResponseEntity<>(DefaultRes.res(StatusCode.BAD_REQUEST, key + " : " + validatorResult.get(key)), HttpStatus.BAD_REQUEST);
                }
            }
            return diaryService.createDiary((String) authentication.getDetails(),diaryDto);
        } catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 내 일기 수정
    @PatchMapping("/diary/{diaryID}")
    public ResponseEntity updateDiary(@Valid @RequestBody updateDiaryDto updateDiaryDto, Errors errors, @PathVariable("diaryID") Long diaryID){
        try {
            if(errors.hasErrors()){
                /* 유효성 통과 못한 필드와 메시지를 핸들링 */
                Map<String, String> validatorResult = validateUtil.validateHandling(errors);
                System.out.println(validatorResult);
                for (String key : validatorResult.keySet()) {
                    System.out.println(key);
                    System.out.println(validatorResult.get(key));
                    return new ResponseEntity<>(DefaultRes.res(StatusCode.BAD_REQUEST, key + " : " + validatorResult.get(key)), HttpStatus.BAD_REQUEST);
                }
            }
            return diaryService.updateDiary(diaryID,updateDiaryDto);
        } catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 내 일기 조회(조회 조건 : startAt, endAt, page, size, sort 가능)
    @GetMapping("/diarys")
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
    @DeleteMapping("/diary/{diaryID}")
    public ResponseEntity getDiarys(@PathVariable("diaryID") Long diaryID) {
        try {
            return diaryService.deleteDiary(diaryID);
        } catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}