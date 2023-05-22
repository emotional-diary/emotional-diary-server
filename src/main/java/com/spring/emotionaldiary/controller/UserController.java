package com.spring.emotionaldiary.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spring.emotionaldiary.dto.LoginDto;
import com.spring.emotionaldiary.dto.SignupDto;
import com.spring.emotionaldiary.dto.SignupRes;
import com.spring.emotionaldiary.dto.SocialUserInfoDto;
import com.spring.emotionaldiary.model.Users;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.service.UserService;
import com.spring.emotionaldiary.utils.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
//@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ValidateUtil validateUtil;

    //회원가입 API
    @PostMapping("/api/v1/users")
    public ResponseEntity signup(@Valid @RequestBody SignupDto signupDto, Errors errors, HttpServletResponse response){
        try{
            if(errors.hasErrors()){
                /* 유효성 통과 못한 필드와 메시지를 핸들링 */
                Map<String, String> validatorResult = validateUtil.validateHandling(errors);
                System.out.println(validatorResult);
                for (String key : validatorResult.keySet()) {
                    System.out.println(key);
                    System.out.println(validatorResult.get(key));
                    return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,key+" : "+validatorResult.get(key)),HttpStatus.BAD_REQUEST);
                }
            }
            return userService.signup(signupDto,response);
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/v1/users/login")
    public ResponseEntity login(@RequestBody LoginDto loginDto,HttpServletResponse response){
        try{
            return userService.login(loginDto,response);
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/auth/kakao/callback")
    public String kakaoLogin(String code,HttpServletResponse response) throws JsonProcessingException { //데이터를 리턴해주는 컨트롤러 함수(@ResponseBody)
        return userService.kakaoLoginService(code,response);
    }
}
