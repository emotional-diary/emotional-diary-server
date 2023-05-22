package com.spring.emotionaldiary;

import com.spring.emotionaldiary.badword.BadWordFiltering;
import com.spring.emotionaldiary.repository.TermsRepository;
import com.spring.emotionaldiary.service.EmailService;
import com.spring.emotionaldiary.utils.RedisUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private EmailService emailService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private TermsRepository termsRepository;


    @Test
    void BadWordFilteringTest(){
        String bad1 = "ㅅ    ㅂ";
        String bad2 = "ㅅㅂ";
        BadWordFiltering badWordFiltering = new BadWordFiltering();

        boolean bool1 = badWordFiltering.check(bad1);         //욕    설
        boolean bool2 = badWordFiltering.blankCheck(bad1);    //욕    설
        boolean bool3 = badWordFiltering.check(bad2);        //욕설
        boolean bool4 = badWordFiltering.blankCheck(bad2);    //욕설
        System.out.println(bool1);
        System.out.println(bool2);
        System.out.println(bool3);
        System.out.println(bool4);
    }

    @Test
    void sendMail() throws Exception {
        // emailService.sendEmailMessage("skarudals27@naver.com");
        System.out.println(redisUtil.getData("skarudals27@naver.com"));
        // Assertions.assertTrue(redisUtil.existData("skarudals27@naver.com"));
        // redisUtil.setDataExpire("skarudals27@naver.com", "233ddd", 60 * 60L);
        // Assertions.assertFalse(redisUtil.existData("skarudals27@naver.com")); //에러 나야함
    }

    @Test
    public void redisTest(){
        //given
        String email = "skarudals27@naver.com";
        String code = "aaa111";

        //when
        redisUtil.setDataExpire(email, code, 60*60L );

        //then
        Assertions.assertTrue(redisUtil.existData(email));
        System.out.println(redisUtil.getData(email));
//        Assertions.assertFalse(redisUtil.existData("test1@test.com"));
//        Assertions.assertEquals(redisUtil.getData(email), "aaa111");
    }

    @Test
    public void termsTest(){
        if (termsRepository.findById((long)1).isPresent()) {
            System.out.println(termsRepository.findById((long)1).get());
        };
    }
}