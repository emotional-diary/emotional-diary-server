package com.spring.emotionaldiary.repository;

import com.spring.emotionaldiary.model.Emotion;
import com.spring.emotionaldiary.model.Tags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Repository
public interface TagsRepository extends JpaRepository<Tags,Long> {
    List<Tags> findAllByEmotion(Emotion emotion);
    Tags findTagsByEmotion(Emotion emotion);
}
