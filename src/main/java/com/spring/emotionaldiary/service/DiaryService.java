package com.spring.emotionaldiary.service;

import com.spring.emotionaldiary.dto.DiarysDto;
import com.spring.emotionaldiary.dto.DiarysRes;
import com.spring.emotionaldiary.dto.updateUserDto;
import com.spring.emotionaldiary.model.Diarys;
import com.spring.emotionaldiary.model.Users;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.repository.DiarysRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiarysRepository diarysRepository;

    @Transactional
    public ResponseEntity getDiarysByDiaryAtAndUserEmail(Date startAt, Date endAt, String email, Pageable diaryPageable) {
        try {
            final DiarysRes diarysRes = new DiarysRes();

            // diaryPageable은 쿼리 파라미터 형식으로 입력 : ex) &page=0&size=2&sort=diaryAt,desc -> sort 여러개도 가능
            Page<Diarys> diarysPages = diarysRepository.findByDiaryAtBetweenAndUsers_Email(startAt, endAt, email, diaryPageable);
            // Entity - Dto 수동 변환 방법
            List<DiarysDto> diarysResList = diarysPages.getContent().stream()
                    .map(d -> new DiarysDto(d))
                    .collect(Collectors.toList());
            diarysRes.setDiarysDtoList(diarysResList);
            diarysRes.setTotalPages(diarysPages.getTotalPages());
            diarysRes.setTotalElements(diarysPages.getTotalElements());

            return new ResponseEntity(DefaultRes.res(StatusCode.OK, ResponseMessage.READ_DIARYS, diarysRes),
                    HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity deleteDiary(Long diaryID){
        try {
            Optional<Diarys> diary = diarysRepository.findById(diaryID);
            if (!diary.isPresent()) {
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, "해당 일기가 존재하지 않습니다."), HttpStatus.BAD_REQUEST);
            }
            diarysRepository.delete(diary.get()); //다이어리 삭제
            return new ResponseEntity(DefaultRes.res(StatusCode.OK, "일기 삭제 완료"), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
