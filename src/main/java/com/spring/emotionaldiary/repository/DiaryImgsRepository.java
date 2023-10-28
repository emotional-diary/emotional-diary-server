package com.spring.emotionaldiary.repository;

import com.spring.emotionaldiary.dto.ImgRes;
import com.spring.emotionaldiary.model.DiaryImgs;
import com.spring.emotionaldiary.model.Diarys;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Repository
public interface DiaryImgsRepository extends JpaRepository<DiaryImgs,Long> {
    List<DiaryImgs> findAllByDiarys(Optional<Diarys> diary);
}
