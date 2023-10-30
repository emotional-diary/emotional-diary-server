package com.spring.emotionaldiary.service;

import com.amazonaws.services.s3.AmazonS3;
import com.spring.emotionaldiary.dto.*;
import com.spring.emotionaldiary.model.AIComments;
import com.spring.emotionaldiary.model.DiaryImgs;
import com.spring.emotionaldiary.model.Diarys;
import com.spring.emotionaldiary.model.Users;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.repository.AICommentsRepository;
import com.spring.emotionaldiary.repository.DiaryImgsRepository;
import com.spring.emotionaldiary.repository.DiarysRepository;
import com.spring.emotionaldiary.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final DiarysRepository diarysRepository;
    private final AICommentsRepository aiCommentsRepository;
    private final UsersRepository usersRepository;
    private final DiaryImgsRepository diaryImgsRepository;
    private final S3UploadService s3Service;

    private final AmazonS3 amazonS3;

    //일기 조건 조회
    @Transactional
    public ResponseEntity getDiarysByDiaryAtAndUserEmail(LocalDate startAt, LocalDate endAt, String email, Pageable diaryPageable) {
        try {
            final DiarysRes diarysRes = new DiarysRes();

            // diaryPageable은 쿼리 파라미터 형식으로 입력 : ex) &page=0&size=2&sort=diaryAt,desc -> sort 여러개도 가능
            Page<Diarys> diarysPages = diarysRepository.findByDiaryAtBetweenAndUsers_Email(startAt, endAt, email, diaryPageable);
            System.out.println(diarysPages);

            // diary별 img 추가
//            List<DiaryImgs> imgsList = diaryImgsRepository.findAllByDiarys(diarysPages);
//            List<ImgRes> imgResList = imgsList.stream()
//                    .map(di -> new ImgRes(di))
//                    .collect(Collectors.toList());

            // Entity - Dto 수동 변환 방법
            List<DiarysDto> diarysResList = diarysPages.getContent().stream()
                    .map(d -> {
                        // diary별 img 추가
                        List<DiaryImgs> imgsList = diaryImgsRepository.findAllByDiarys(Optional.ofNullable(d));
                        List<ImgRes> imgResList = imgsList.stream()
                                .map(di -> new ImgRes(di))
                                .collect(Collectors.toList());
                        DiarysDto dto = new DiarysDto(d);
                        dto.setImages(imgResList);
                        return dto;
                    })
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

    // 일기 삭제
    @Transactional
    public ResponseEntity deleteDiary(Long diaryID){
        try {
            Optional<Diarys> diary = diarysRepository.findById(diaryID);
            if (!diary.isPresent()) {
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, "해당 일기가 존재하지 않습니다."), HttpStatus.BAD_REQUEST);
            }
            //DiaryImgs에서 해당 s3 filePath 가져와 S3에서 파일 삭제
            List<DiaryImgs> diaryImgsList = diaryImgsRepository.findAllByDiarys(diary);
            System.out.println(diaryImgsList);

            diaryImgsList.forEach(di -> {
                String imgUrl = di.getImageUrl();
                // s3 이미지의 key -> 'bucket/' 뒤의 값
                String key = imgUrl.substring(imgUrl.indexOf(bucket) + bucket.length() + 1);

                // S3 객체의 존재 여부를 확인하고 삭제
                boolean isObjectExist = amazonS3.doesObjectExist(bucket, key);
                System.out.println(isObjectExist);

                if (isObjectExist) {
                    amazonS3.deleteObject(bucket, key);
                    System.out.println("Deleted: " + key);
                }
            });

            diarysRepository.delete(diary.get()); //다이어리 삭제
            return new ResponseEntity(DefaultRes.res(StatusCode.OK, "일기 삭제 완료"), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 일기 상세 조회
    @Transactional
    public ResponseEntity selectDiary(Long diaryID){
        try {
            Optional<Diarys> diary = diarysRepository.findById(diaryID);
            if (!diary.isPresent()) {
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, "해당 일기가 존재하지 않습니다."), HttpStatus.BAD_REQUEST);
            }
            // diary별 img 추가
            List<DiaryImgs> imgsList = diaryImgsRepository.findAllByDiarys(diary);
            List<ImgRes> imgResList = imgsList.stream()
                    .map(di -> new ImgRes(di))
                    .collect(Collectors.toList());

            // Entity - Dto 수동 변환 방법
            List<DiarysDto> diarysResList = diary.stream()
                    .map(d -> {
                        DiarysDto dto = new DiarysDto(d);
                        dto.setImages(imgResList);
                        return dto;
                    })
                    .collect(Collectors.toList());

            return new ResponseEntity(DefaultRes.res(StatusCode.OK, "일기 조회 성공",diarysResList), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    // 내 일기 생성
    public ResponseEntity createDiary(String userEmail,DiaryDto diaryDto,List<String> imgPaths){
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

                    DiarysDto diarysDto = new DiarysDto(diarys);

                    // 4.DiaryImgs 테이블에 s3에 저장된 이미지 리스트 저장
                    if(imgPaths != null){
                        List<ImgRes> imgList = new ArrayList<>();
                        for (String imgUrl : imgPaths){
                            DiaryImgs diaryImgs = new DiaryImgs(imgUrl,diarys);
                            diaryImgsRepository.save(diaryImgs);
                            ImgRes imgRes = new ImgRes(diaryImgs);
                            imgList.add(imgRes);
                        }
                        diarysDto.setImages(imgList);
                    }

                    return new ResponseEntity(DefaultRes.res(StatusCode.OK, "일기 생성 성공",diarysDto), HttpStatus.OK);
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
    public ResponseEntity updateDiary(Long diaryID,updateDiaryDto updateDiaryDto,List<String> imgPaths){
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
            // 일기 날짜 수정의 경우
            if (updateDiaryDto.getDiaryAt() != null) {
                diary.get().setDiaryAt(updateDiaryDto.getDiaryAt());
            }
            // 감정 수정 경우
            if (updateDiaryDto.getEmotion() != null) {
                diary.get().setEmotion(updateDiaryDto.getEmotion());
            }
            // 삭제한 이미지가 있는 경우
            System.out.println(updateDiaryDto.getDeleteImgIDList());
            if(updateDiaryDto.getDeleteImgIDList() != null){ // 삭제된 이미지가 존재하는 경우
                updateDiaryDto.getDeleteImgIDList().forEach(imgID -> {
                    System.out.println(imgID);
                    String imgUrl = diaryImgsRepository.findById(imgID).get().getImageUrl();
                    // s3 이미지의 key -> 'bucket/' 뒤의 값
                    String key = imgUrl.substring(imgUrl.indexOf(bucket) + bucket.length() + 1);

                    // S3 객체의 존재 여부를 확인하고 삭제
                    boolean isObjectExist = amazonS3.doesObjectExist(bucket, key);
                    if (isObjectExist) {
                        amazonS3.deleteObject(bucket, key); //S3 이미지 파일 삭제
                        System.out.println("Deleted: " + key);
                        diaryImgsRepository.deleteById(imgID); //테이블 데이터 삭제
                    }
                });
            }
            // 추가한 이미지가 있는 경우
            // DiaryImgs 테이블에 s3에 저장된 이미지 리스트 저장
            if(imgPaths != null){
                for (String imgUrl : imgPaths){
                    DiaryImgs diaryImgs = new DiaryImgs(imgUrl,diary.get());
                    diaryImgsRepository.save(diaryImgs);
                    ImgRes imgRes = new ImgRes(diaryImgs);
                }
            }
            diary.get().setMetaData(updateDiaryDto.getMetaData());

            // Entity - Dto 수동 변환 방법
            DiarysDto diarysDto = new DiarysDto(diary.get());
            List<ImgRes> imgList = new ArrayList<>();
            List<DiaryImgs> diaryImgsList = diaryImgsRepository.findAllByDiarys(diary);
            diaryImgsList.forEach(di -> {
                ImgRes imgRes = new ImgRes(di);
                imgList.add(imgRes);
            });
            diarysDto.setImages(imgList);

            return new ResponseEntity(DefaultRes.res(StatusCode.OK,ResponseMessage.UPDATE_DIARY,diarysDto),HttpStatus.OK);
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
                //"http://175.116.178.86:5000/api/model/predict",
                 "http://127.0.0.1:8001/api/model/predict",
                HttpMethod.POST,
                requestEntity,
                AIComentRes.class
        );

        return response.getBody();
    }
}
