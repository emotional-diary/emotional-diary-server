package com.spring.emotionaldiary.controller;

import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.dto.EmailCodeReq;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;

@RestController
public class EmailController {
    @Autowired
    private EmailService emailService;

    @PostMapping("check-email")
    public ResponseEntity sendEmailPath(@RequestBody EmailCodeReq emailCodeReq) throws Exception {
        emailService.sendEmailMessage(emailCodeReq.getEmail());
        return new ResponseEntity(DefaultRes.res(StatusCode.OK, ResponseMessage.SEND_EMAIL_AUTHENTICATION_CODE), HttpStatus.OK);
    }

    @PostMapping("/check-email/code")
    public ResponseEntity sendEmailAndCode(@RequestBody EmailCodeReq emailCodeReq) throws Exception {
        if (emailService.getUserIdByEmail(emailCodeReq.getEmail(),emailCodeReq.getCode())) {
            return new ResponseEntity(DefaultRes.res(StatusCode.OK,"이메일 인증 성공"),HttpStatus.OK);
        }
        return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,"실패"),HttpStatus.BAD_REQUEST);
    }
}
