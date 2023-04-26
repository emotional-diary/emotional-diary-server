package com.spring.emotionaldiary.controller;

import com.spring.emotionaldiary.dto.SignupDto;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.dto.LoginReq;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

//    @PostMapping("login")
//    public ResponseEntity login(@RequestBody LoginReq loginReq){
//        return new ResponseEntity(DefaultRes.res(StatusCode.OK,
//                ResponseMessage.LOGIN_SUCCESS,loginReq), HttpStatus.OK);
//    }

    @PostMapping("signup")
    public ResponseEntity signup(@Valid @RequestBody SignupDto signupDto, Errors errors){
        if(errors.hasErrors()){
            /* 유효성 통과 못한 필드와 메시지를 핸들링 */
            Map<String, String> validatorResult = userService.validateHandling(errors);
            for (String key : validatorResult.keySet()) {
                System.out.println(key);
                System.out.println(validatorResult.get(key));
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,key+" : "+validatorResult.get(key)),HttpStatus.BAD_REQUEST);
            }
        }
        return userService.signup(signupDto);
    }
}
