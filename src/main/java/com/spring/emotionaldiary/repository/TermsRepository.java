package com.spring.emotionaldiary.repository;

import com.spring.emotionaldiary.model.Terms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
@Repository
public interface TermsRepository extends JpaRepository<Terms,Long> {
    Optional<Terms> findById(Long id);
}
