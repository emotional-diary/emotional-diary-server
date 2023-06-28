package com.spring.emotionaldiary.repository;

import com.spring.emotionaldiary.model.Diarys;
import com.spring.emotionaldiary.model.Emotion;
import com.spring.emotionaldiary.model.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional(readOnly = true)
@Repository
public interface DiarysRepository extends JpaRepository<Diarys,Long> {
//    List<Diarys> findByDiaryAtBetweenAndUsers_Email(Date startAt, Date endAt,String email);
    Page<Diarys> findByDiaryAtBetweenAndUsers_Email(Date startAt, Date endAt, String email, Pageable pageable);

}
