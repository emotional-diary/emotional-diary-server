package com.spring.emotionaldiary.controller;

import com.spring.emotionaldiary.model.Users;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.dto.EmailAuthenticationDto;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.repository.UsersRepository;
import com.spring.emotionaldiary.service.EmailService;
import com.spring.emotionaldiary.service.UserService;
import com.spring.emotionaldiary.utils.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RequestMapping("/api/v1/users")
@RestController
public class EmailController {
    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;
    @Autowired
    private ValidateUtil validateUtil;

    //이메일로 유저정보 조회
    @PostMapping("/email")
    public ResponseEntity findByEmail(@RequestBody EmailAuthenticationDto emailCodeReq) throws Exception{
        try{
            return emailService.findByEmail(emailCodeReq.getEmail());
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 이메일 유효성 인증
    @PostMapping("/email-validation")
    public ResponseEntity sendEmailPath(@Valid @RequestBody EmailAuthenticationDto emailCodeReq, Errors errors) throws Exception {
        try{
            if(errors.hasErrors()){
                /* 유효성 통과 못한 필드와 메시지를 핸들링 */
                Map<String, String> validatorResult = validateUtil.validateHandling(errors);
                for (String key : validatorResult.keySet()) {
                    System.out.println(key);
                    System.out.println(validatorResult.get(key));
                    return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,key+" : "+validatorResult.get(key)),HttpStatus.BAD_REQUEST);
                }
            }
            return emailService.sendEmailMessage(emailCodeReq.getEmail());
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/email-validation/check")
    public ResponseEntity sendEmailAndCode(@Valid @RequestBody EmailAuthenticationDto emailCodeReq) throws Exception {
        if (emailService.getuserIDByEmail(emailCodeReq.getEmail(),emailCodeReq.getCode())) {
            return new ResponseEntity(DefaultRes.res(StatusCode.OK,"이메일 인증 성공"),HttpStatus.OK);
        }
        return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,"잘못된 인증코드 입니다."),HttpStatus.BAD_REQUEST);
    }
}
