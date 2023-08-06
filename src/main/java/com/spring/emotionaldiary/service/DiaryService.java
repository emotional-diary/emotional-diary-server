package com.spring.emotionaldiary.service;

import com.spring.emotionaldiary.dto.*;
import com.spring.emotionaldiary.model.AIComments;
import com.spring.emotionaldiary.model.Diarys;
import com.spring.emotionaldiary.model.Users;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.repository.AICommentsRepository;
import com.spring.emotionaldiary.repository.DiarysRepository;
import com.spring.emotionaldiary.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
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
    private final AICommentsRepository aiCommentsRepository;
    private final UsersRepository usersRepository;
    @Transactional
    public ResponseEntity getDiarysByDiaryAtAndUserEmail(Date startAt, Date endAt,String email, Pageable diaryPageable) {
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

    @Transactional
    public ResponseEntity selectDiary(Long diaryID){
        try {
            Optional<Diarys> diary = diarysRepository.findById(diaryID);
            if (!diary.isPresent()) {
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, "해당 일기가 존재하지 않습니다."), HttpStatus.BAD_REQUEST);
            }
            // Entity - Dto 수동 변환 방법
            List<DiarysDto> diarysResList = diary.stream()
                    .map(d -> new DiarysDto(d))
                    .collect(Collectors.toList());

            return new ResponseEntity(DefaultRes.res(StatusCode.OK, "일기 조회 성공",diarysResList), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    // 내 일기 생성
    public ResponseEntity createDiary(String userEmail,DiaryDto diaryDto){
        try {
            // 해당 user 유효한지 확인
            Optional<Users> user = usersRepository.findByEmail(userEmail);
            if(!user.isPresent()){
                return new ResponseEntity(DefaultRes.res(StatusCode.UNAUTHORIZED, ResponseMessage.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
            }
            ModelInfo modelInfo = new ModelInfo();
            modelInfo.setText(diaryDto.getContent());

            //1. front에서 받은 diaryDto.content를 AI에게 전달 및 answer 받아옴
            AIComentRes aiComentRes = AIEmotionalAnalysis(modelInfo);

            // AIComment가 성공적으로 전달되었을 때 200 뜸
            if(aiComentRes.getStatusCode() == 200){
                try {
                    // 2. answer값 ai_comments 테이블에 저장
                    AIComments aiComments = aiComentRes.toAIComments();
                    AIComments saveAIComments = aiCommentsRepository.save(aiComments);

                    // 3. commentID와 함께 diary 테이블에 저장
                    SaveDiarysDto saveDiarysDto = new SaveDiarysDto(diaryDto.getContent(),diaryDto.getEmotion(),diaryDto.getMetaData(),user,saveAIComments,diaryDto.getDiaryAt());
                    Diarys diarys = saveDiarysDto.toDiarys();
                    diarysRepository.save(diarys);

                    return new ResponseEntity(DefaultRes.res(StatusCode.OK, "일기 생성 성공"), HttpStatus.OK);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }else{
                // 다른 statusCode return response 코드 구현
                return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
            }

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //내 일기 수정
    @Transactional
    public ResponseEntity updateDiary(Long diaryID,updateDiaryDto updateDiaryDto){
        try{
            Optional<Diarys> diary = diarysRepository.findById(diaryID);
            if(!diary.isPresent()){
                return new ResponseEntity(DefaultRes.res(StatusCode.NOT_FOUND, ResponseMessage.READ_DIARYS_FAIL), HttpStatus.NOT_FOUND);
            }
            System.out.println(updateDiaryDto.getContent());
            // content를 수정한 경우
            if(updateDiaryDto.getContent() != null){
                // 일기 content 수정
                diary.get().setContent(updateDiaryDto.getContent());

                ModelInfo modelInfo = new ModelInfo();
                modelInfo.setText(updateDiaryDto.getContent());

                //1. front에서 받은 diaryDto.content를 AI에게 전달 및 answer 받아옴
                AIComentRes aiComentRes = AIEmotionalAnalysis(modelInfo);

                // AIComment가 성공적으로 전달되었을 때 200 뜸
                if(aiComentRes.getStatusCode() == 200){
                    try {
                        // 2. answer값 ai_comments 테이블에 저장
                        AIComments aiComments = aiComentRes.toAIComments();
                        AIComments saveAIComments = aiCommentsRepository.save(aiComments);

                        // 3. diarys 테이블의 ComentID 변경
                        diary.get().setAiComments(saveAIComments);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }else{
                    // 다른 statusCode return response 코드 구현
                    return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            if (updateDiaryDto.getDiaryAt() != null) {
                diary.get().setDiaryAt(updateDiaryDto.getDiaryAt());
            }

            if (updateDiaryDto.getEmotion() != null) {
                diary.get().setEmotion(updateDiaryDto.getEmotion());
            }

            diary.get().setMetaData(updateDiaryDto.getMetaData());
            return new ResponseEntity(DefaultRes.res(StatusCode.OK,ResponseMessage.UPDATE_DIARY),HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // SpringBoot 와 FLASK server to server 통신 - AI Comments 전달받음
    private AIComentRes AIEmotionalAnalysis(ModelInfo modelInfo){
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<ModelInfo> requestEntity = new HttpEntity<>(modelInfo, headers);
        RestTemplate rt = new RestTemplate();

        ResponseEntity<AIComentRes> response = rt.exchange(
                "http://175.116.178.86:5000/api/model/predict",
                HttpMethod.POST,
                requestEntity,
                AIComentRes.class
        );

        return response.getBody();
    }
}
