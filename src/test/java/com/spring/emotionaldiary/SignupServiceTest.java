package com.spring.emotionaldiary;

import com.spring.emotionaldiary.repository.TermsRepository;
import com.spring.emotionaldiary.repository.UserTermsRepository;
import net.minidev.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SignupServiceTest {

    @Autowired
    private TermsRepository termsRepository;

}
