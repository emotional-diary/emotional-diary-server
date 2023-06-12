package com.spring.emotionaldiary.service;

import com.spring.emotionaldiary.model.Emotion;
import com.spring.emotionaldiary.model.Tags;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.repository.TagsRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TagsService {
    private final TagsRepository tagsRepository;

    // 감정별 태그 조회
    @Transactional
    public ResponseEntity readTagsByEmotion(Emotion emotion){
        try{
            List<Tags> tags = tagsRepository.findAllByEmotion(emotion);
            return new ResponseEntity(DefaultRes.res(StatusCode.OK,ResponseMessage.READ_USER,tags),HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
