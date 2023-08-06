package com.spring.emotionaldiary.repository;

import com.spring.emotionaldiary.model.AIComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Repository
public interface AICommentsRepository extends JpaRepository<AIComments,Long> {
}
