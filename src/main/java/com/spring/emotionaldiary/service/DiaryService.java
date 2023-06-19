package com.spring.emotionaldiary.service;

import com.spring.emotionaldiary.dto.DiarysRes;
import com.spring.emotionaldiary.model.Diarys;
import com.spring.emotionaldiary.repository.DiarysRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiarysRepository diarysRepository;

    @Transactional
    public List<Diarys> getDiarysByDiaryAt(Date startAt, Date endAt){
        List<Diarys> diarysList = diarysRepository.findByDiaryAtBetween(startAt, endAt);
//        List<DiarysRes> diarysResList = new ArrayList<>();
//
//        for (Diarys diary : diarysList) {
//            DiarysRes diarysRes = new DiarysRes(diary.getDiaryID(),diary.getTitle(),diary.getContent(),diary.getImageUrl(),diary.getEmotion(),diary.getMetaData(),diary.getUsers().getUserId(),diary.getUsers().getName(),diary.getAiComments().getCommentID(),diary.getAiComments().getComment(),diary.getDiaryAt());
//            diarysResList.add(diarysRes);
//        }
        return diarysList;
    }
}
