package com.spring.emotionaldiary.service;

import com.spring.emotionaldiary.model.Users;
import com.spring.emotionaldiary.until.RedisUtil;
import com.spring.emotionaldiary.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;
    private final RedisUtil redisUtil;
    // private final EmailProperties emailProperties;
    private final UsersRepository usersRepository;

    @Value("${spring.mail.username}")
    private String configEmail;

//    public void sendEmailMessage(String email) throws Exception {
//        String code = createCode(); // 인증코드 생성
//        MimeMessage message = emailSender.createMimeMessage();
//
//        message.addRecipients(MimeMessage.RecipientType.TO, email); // 보낼 이메일 설정
//        message.setSubject("[인증 코드] " + code); // 이메일 제목
//        message.setText(setContext(code), "utf-8", "html"); // 내용 설정(Template Process)
//
//        // 보낼 때 이름 설정하고 싶은 경우
//        // message.setFrom(new InternetAddress([이메일 계정], [설정할 이름]));
//
//        emailSender.send(message); // 이메일 전송
//    }

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

    public void sendEmailMessage(String email) throws Exception {
        if (redisUtil.existData(email)) {
            redisUtil.deleteData(email);
        }
        System.out.println(redisUtil.existData(email));
        MimeMessage message = createMessage(email);
        emailSender.send(message);
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
