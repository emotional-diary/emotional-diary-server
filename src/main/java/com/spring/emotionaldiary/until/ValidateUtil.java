package com.spring.emotionaldiary.until;

import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

public class ValidateUtil {

    @Autowired
    private Validator validator;

    public void validate(Object entity){
        Set<ConstraintViolation<Object>> violationSet = validator.validate(entity);
    }
}
