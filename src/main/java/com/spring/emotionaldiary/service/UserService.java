package com.spring.emotionaldiary.service;

import com.spring.emotionaldiary.badword.BadWordFiltering;
import com.spring.emotionaldiary.dto.SignupDto;
import com.spring.emotionaldiary.model.UserTerms;
import com.spring.emotionaldiary.model.Users;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.repository.UserTermsRepository;
import com.spring.emotionaldiary.repository.UsersRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UsersRepository usersRepository;
    private final UserTermsRepository userTermsRepository;
    BadWordFiltering badWordFiltering = new BadWordFiltering();

    public UserService(UsersRepository usersRepository, UserTermsRepository userTermsRepository) {
        this.usersRepository = usersRepository;
        this.userTermsRepository = userTermsRepository;
    }

    public ResponseEntity signup(SignupDto signUp) {
        // 데이터베이스 저장 중에 발생할 수 있는 에러를 처리
        try {
            // 해당 이메일 계정이 이미 존재하는지 확인
            if (usersRepository.existsByEmail(signUp.getEmail())) {
                return new ResponseEntity(DefaultRes.res(StatusCode.CONFLICT, ResponseMessage.DUPLICATE_EMAIL),
                        HttpStatus.CONFLICT);
            }
            if(badWordFiltering.blankCheck(signUp.getName())){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, "비속어, 욕설은 사용 불가합니다."),
                        HttpStatus.BAD_REQUEST);
            }
            // Users 객체 생성 및 저장
            Users user = signUp.toUser();
            Users savedUser = usersRepository.save(user);

            // UserTerms 객체 생성 및 저장
            // List<UserTerms> userTermsList = signUp.toUserTerms();
            // userTermsList.forEach(userTerms -> userTerms.setUsers(savedUser));
            // userTermsRepository.saveAll(userTermsList);
            return new ResponseEntity(DefaultRes.res(StatusCode.CREATED, ResponseMessage.CREATED_USER), HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /* 회원가입 시, 유효성 체크 */
    @Transactional(readOnly = true)
    public Map<String, String> validateHandling(Errors errors) {
        Map<String, String> validatorResult = new HashMap<>();

        /* 유효성 검사에 실패한 필드 목록을 받음 */
        for (FieldError error : errors.getFieldErrors()) {
            String validKeyName = String.format("valid_%s", error.getField());
            validatorResult.put(validKeyName, error.getDefaultMessage());
        }
        return validatorResult;
    }
}
