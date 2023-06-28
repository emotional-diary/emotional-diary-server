package com.spring.emotionaldiary.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DiarysRes {
    private List<DiarysDto> diarysDtoList;
    private Integer totalPages;
    private Long totalElements;

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public void setDiarysDtoList(List<DiarysDto> diarysDtoList) {
        this.diarysDtoList = diarysDtoList;
    }
}
