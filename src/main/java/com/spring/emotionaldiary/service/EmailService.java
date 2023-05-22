package com.spring.emotionaldiary.service;

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
        return templateEngine.process("mail", context); // mail.html
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

    public ResponseEntity sendEmailMessage(String email) throws Exception {
        // 해당 이메일 계정이 이미 존재하는지 확인
//        if (usersRepository.existsByEmail(email)) {
//            Users user = usersRepository.findByEmail(email);
//            return new ResponseEntity(DefaultRes.res(StatusCode.CONFLICT,user.getLoginType() + "로 가입된 회원입니다."),
//                    HttpStatus.CONFLICT);
//        }
        //해당 이메일이 Redis에 저장되어 있을 시, 삭제하고 등록
        if (redisUtil.existData(email)) {
            redisUtil.deleteData(email);
        }
        // System.out.println(redisUtil.existData(email));
        MimeMessage message = createMessage(email);
        emailSender.send(message);
        return new ResponseEntity(DefaultRes.res(StatusCode.OK, ResponseMessage.SEND_EMAIL_AUTHENTICATION_CODE), HttpStatus.OK);
    }

    public Boolean getUserIdByEmail(String email,String code) {
        String validCode = redisUtil.getData(email); // 입력 받은 인증 코드(key)를 이용해 email(value)을 꺼낸다.
        if(validCode.equals(code)){
            return true;
        }
        return false;
        // Users user = usersRepository.findByEmail(email); // 해당 email로 user를 꺼낸다.
    }
}
