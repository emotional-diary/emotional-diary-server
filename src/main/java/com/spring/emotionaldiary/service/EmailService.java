package com.spring.emotionaldiary.service;

import com.spring.emotionaldiary.model.LoginType;
import com.spring.emotionaldiary.model.Users;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.utils.RedisUtil;
import com.spring.emotionaldiary.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;
    private final RedisUtil redisUtil;
    private final UsersRepository usersRepository;

    @Value("${spring.mail.username}")
    private String configEmail;

    private String setContext(String code) { // 타임리프 설정하는 코드
        Context context = new Context();
        context.setVariable("code", code); // Template에 전달할 데이터 설정
        return templateEngine.process("email_authcode_form", context); // email_authcode_form.html
    }

    private String createCode() {
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 7; i++) {
            int rIndex = rnd.nextInt(3);
            switch (rIndex) {
                case 0:
                    code.append((char) (rnd.nextInt(26) + 97));
                    break;
                case 1:
                    code.append((char) (rnd.nextInt(26) + 65));
                    break;
                case 2:
                    code.append((rnd.nextInt(10)));
                    break;
            }
        }
        return code.toString();
    }

    private MimeMessage createMessage(String email) throws Exception {
        String code = createCode();

        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email); // 보낼 이메일 설정
        message.setSubject("[인증 코드] " + code); // 이메일 제목
        message.setText(setContext(code), "utf-8", "html"); // 내용 설정(Template Process)
        System.out.println(code);
        redisUtil.setDataExpire(email,code, 60*5L);
        return message;
        // 보낼 때 이름 설정하고 싶은 경우
        // message.setFrom(new InternetAddress([이메일 계정], [설정할 이름]));
    }

    public ResponseEntity findByEmail(String email) throws Exception{
        // 이메일로 유저 정보 조회
        try{
            Optional<Users> user = usersRepository.findByEmail(email);
            // user가 있는데 loginType이 USER가 아니면 에러처리
            if(user.isPresent() && user.get().getLoginType()!= LoginType.LOCAL){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,user.get().getLoginType()+" 계정으로 가입했습니다."),HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity(DefaultRes.res(StatusCode.OK,"이메일로 유저 정보 조회 성공",user),HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity sendEmailMessage(String email) throws Exception {
        //해당 이메일이 Redis에 저장되어 있을 시, 삭제하고 등록
        if (redisUtil.existData(email)) {
            redisUtil.deleteData(email);
        }
        // System.out.println(redisUtil.existData(email));
        MimeMessage message = createMessage(email);
        emailSender.send(message);
        return new ResponseEntity(DefaultRes.res(StatusCode.OK, ResponseMessage.SEND_EMAIL_AUTHENTICATION_CODE), HttpStatus.OK);
    }

    public Boolean getuserIDByEmail(String email,String code) {
        String validCode = redisUtil.getData(email); // 입력 받은 인증 코드(key)를 이용해 email(value)을 꺼낸다.
        if(validCode.equals(code)){
            return true;
        }
        return false;
        // Users user = usersRepository.findByEmail(email); // 해당 email로 user를 꺼낸다.
    }
}
