package com.spring.emotionaldiary.controller;

import com.spring.emotionaldiary.dto.DiaryDto;
import com.spring.emotionaldiary.dto.DiarysDto;
import com.spring.emotionaldiary.dto.updateDiaryDto;
import com.spring.emotionaldiary.dto.updateUserDto;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.service.DiaryService;
import com.spring.emotionaldiary.service.S3UploadService;
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
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/v1/users")
@RestController
@Slf4j
@RequiredArgsConstructor
public class DiaryController {
    @Autowired
    private DiaryService diaryService;
    @Autowired
    private ValidateUtil validateUtil;

    private final S3UploadService s3Service;

    // 내 일기 작성
    @PostMapping("/diary")
    // 파일 전송시 @RequestBody가 아니라 @RequestPart 어노테이션을 사용
    public ResponseEntity createDiary(@Valid @RequestPart("diary") DiaryDto diaryDto, @RequestPart(value = "imgUrl",required = false) List<MultipartFile> multipartFiles, Errors errors, Authentication authentication){
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
            System.out.println(ChronoLocalDate.from(LocalDateTime.now(ZoneId.of("Asia/Seoul"))));
            if(diaryDto.getDiaryAt().isAfter(ChronoLocalDate.from(LocalDateTime.now(ZoneId.of("Asia/Seoul"))))){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, "과거와 현재의 일기만 작성이 가능합니다."), HttpStatus.BAD_REQUEST);
            }
            //s3에 이미지 저장
            if(multipartFiles != null){
                List<String> imgPaths = s3Service.upload(multipartFiles);
                System.out.println("IMG 경로들 : " + imgPaths);
                return diaryService.createDiary((String) authentication.getDetails(),diaryDto,imgPaths);
            }
            // 이미지 없는경우 null로 전달
            return diaryService.createDiary((String) authentication.getDetails(),diaryDto,null);
        } catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 내 일기 수정
    @PatchMapping("/diary/{diaryID}")
    public ResponseEntity updateDiary(@Valid @RequestPart("diary") updateDiaryDto updateDiaryDto,
                                      @RequestPart(value = "imgUrl",required = false) List<MultipartFile> multipartFiles,
                                      @PathVariable("diaryID") Long diaryID,Errors errors){
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
            // System.out.println(ChronoLocalDate.from(LocalDateTime.now(ZoneId.of("Asia/Seoul"))));
            if(updateDiaryDto.getDiaryAt().isAfter(ChronoLocalDate.from(LocalDateTime.now(ZoneId.of("Asia/Seoul"))))){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, "과거와 현재의 일기만 작성이 가능합니다."), HttpStatus.BAD_REQUEST);
            }
            //s3에 이미지 저장
            if(multipartFiles != null){
                List<String> imgPaths = s3Service.upload(multipartFiles);
                System.out.println("IMG 경로들 : " + imgPaths);
                return diaryService.updateDiary(diaryID,updateDiaryDto,imgPaths);
            }
            // 이미지 없는경우 null로 전달
            return diaryService.updateDiary(diaryID,updateDiaryDto,null);
        } catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 내 일기 조회(조회 조건 : startAt, endAt, page, size, sort 가능)
    @GetMapping("/diarys")
    public ResponseEntity getDiarys(@RequestParam(required = false) String startAt, @RequestParam(required = false) String endAt, Pageable diaryPageable, Authentication authentication) {
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            LocalDate startDate = null;
            LocalDate endDate = null;
            if (startAt == null && endAt == null) {
                // startAt과 endAt이 모두 null인 경우, 전체 기간으로 설정
                startDate = LocalDate.parse("1900-01-01");
                endDate = LocalDate.now();
            } else if (startAt == null) {
                // startAt이 null인 경우, endAt을 기준으로 startDate 설정
                startDate = LocalDate.parse("1900-01-01");
                endDate = LocalDate.parse(endAt);
            } else if (endAt == null) {
                // endAt이 null인 경우, startAt을 기준으로 endDate 설정
                startDate = LocalDate.parse(startAt);
                endDate = LocalDate.now();
            } else {
                startDate = LocalDate.parse(startAt);
                endDate = LocalDate.parse(endAt);
            }
            System.out.println(endDate);

            return diaryService.getDiarysByDiaryAtAndUserEmail(startDate, endDate,(String) authentication.getDetails(),diaryPageable);
        } catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 일기 상세 조회(diaryID별 조회)
    @GetMapping("/diary/{diaryID}")
    public ResponseEntity selectDiary(@PathVariable("diaryID") Long diaryID){
        try {
            return diaryService.selectDiary(diaryID);
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