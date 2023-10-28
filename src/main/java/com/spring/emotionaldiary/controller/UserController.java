package com.spring.emotionaldiary.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spring.emotionaldiary.dto.*;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.service.UserService;
import com.spring.emotionaldiary.utils.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    @PostMapping("/api/v1/users/signup")
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

    // 회원정보 조회 (jwt claims에 저장한 userEmail 불러와서 정보 조회)
    @GetMapping("/api/v1/users")
    public ResponseEntity readUser(Authentication authentication){
        try{
            //authentication.getDetails는 jwt에 넣은 userEmail
            return userService.readUser((String) authentication.getDetails());
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 회원정보 수정 (jwt claims에 저장한 userEmail 불러와서 정보 조회)
    @PatchMapping("/api/v1/users")
    public ResponseEntity updateUser(@RequestBody updateUserDto updateUserDto, Authentication authentication){
        try{
            //authentication.getDetails는 jwt에 넣은 userEmail
            return userService.updateUser((String) authentication.getDetails(),updateUserDto);
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 비밀번호 변경
    @PatchMapping("/api/v1/users/change-pwd")
    public ResponseEntity changePwd(@RequestBody PasswordDto PasswordDto, Authentication authentication){
        try{
            if(PasswordDto.getNewPassword() == ""){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,"비밀번호를 입력해주세요"), HttpStatus.BAD_REQUEST);
            }
            return userService.changePwd((String) authentication.getDetails(), PasswordDto.getNewPassword());
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 이전 비밀번호와 일치 여부 확인
    @PostMapping("/api/v1/users/verify-pwd")
    public ResponseEntity verifyPwd(@RequestBody PasswordDto PasswordDto, Authentication authentication){
        try{
            if(PasswordDto.getNewPassword() == ""){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,"비밀번호를 입력해주세요"), HttpStatus.BAD_REQUEST);
            }
            return userService.verifyPwd((String) authentication.getDetails(), PasswordDto.getPassword());
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 회원탈퇴 (jwt claims에 저장한 userEmail 불러와서 정보 조회)
    @DeleteMapping("/api/v1/users")
    public ResponseEntity WithdrawalUser(HttpServletRequest servletRequest,Authentication authentication){
        try{
            //authentication.getDetails는 jwt에 넣은 userEmail
            return userService.WithdrawalUser(servletRequest.getHeader(HttpHeaders.AUTHORIZATION).substring(7),(String) authentication.getDetails());
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 로컬 로그인
    @PostMapping("/api/v1/users/login/local")
    public ResponseEntity login(@RequestBody LoginDto loginDto,HttpServletResponse response){
        try{
            return userService.login(loginDto,response);
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

     //로그아웃
    @GetMapping("/api/v1/users/logout")
    public ResponseEntity logout(HttpServletRequest servletRequest){
        try{
            return userService.logout(servletRequest.getHeader(HttpHeaders.AUTHORIZATION).substring(7));
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 비밀번호 찾기
    @PatchMapping("/api/v1/users/find-pwd")
    public ResponseEntity findPwd(@RequestBody PasswordDto PasswordDto){
        try{
            return userService.findPwd(PasswordDto);
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 카카오 소셜 로그인(카카오 정보 받아오는 기능까지)
    @GetMapping("/api/v1/users/login/kakao")
    public ResponseEntity kakaoLogin(@RequestParam String code,HttpServletResponse response) { //데이터를 리턴해주는 컨트롤러 함수(@ResponseBody)
        try{
            System.out.println(code);
            return userService.kakaoLoginService(code,response);
        }catch(Exception e){
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
