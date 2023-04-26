package com.spring.emotionaldiary.repository;

import com.spring.emotionaldiary.model.UserTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Repository
public interface UserTermsRepository extends JpaRepository<UserTerms,Long> {
    @Override
    <S extends UserTerms> List<S> saveAll(Iterable<S> entities);
}
