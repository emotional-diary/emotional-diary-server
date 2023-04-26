package com.spring.emotionaldiary.repository;

import com.spring.emotionaldiary.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

// JPA Data 이용할 때는 항상 인터페이스로 만들어야 하는 것
// Transactional 애노테이션은 항상 스프링 프레임워크에 있는거 쓰자. (기능이 더 많음)
// readOnly 를 통해 write 에 쓰이는 lock 에 대한 연산을 없애서 성능을 약간이라도 최적화
@Transactional(readOnly = true)
@Repository
public interface UsersRepository extends JpaRepository<Users,Long> {
    boolean existsByEmail(String email);
    Users findByEmail(String email);
}
