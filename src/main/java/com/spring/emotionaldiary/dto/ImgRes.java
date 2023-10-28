package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.DiaryImgs;
import lombok.Getter;

@Getter
public class ImgRes {
    private Long diaryImgID;
    private String imgUrl;

    public ImgRes(DiaryImgs di) {
        diaryImgID = di.getDiaryImgID();
        imgUrl = di.getImgUrl();
    }
}
