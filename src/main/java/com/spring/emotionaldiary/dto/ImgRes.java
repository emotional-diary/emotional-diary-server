package com.spring.emotionaldiary.dto;

import com.spring.emotionaldiary.model.DiaryImgs;
import lombok.Getter;

@Getter
public class ImgRes {
    private Long imageID;
    private String imageUrl;

    public ImgRes(DiaryImgs di) {
        imageID = di.getImageID();
        imageUrl = di.getImageUrl();
    }
}
