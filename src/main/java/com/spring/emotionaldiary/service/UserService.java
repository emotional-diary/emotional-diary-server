package com.spring.emotionaldiary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.emotionaldiary.badword.BadWordFiltering;
import com.spring.emotionaldiary.dto.*;
import com.spring.emotionaldiary.model.*;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.repository.TermsRepository;
import com.spring.emotionaldiary.repository.UserTermsRepository;
import com.spring.emotionaldiary.repository.UsersRepository;
import com.spring.emotionaldiary.utils.JwtUtil;
import com.spring.emotionaldiary.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class UserService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${spring.mail.username}")
    private String onedoitEmail;

    private final String SERVER = "Server";
    private final long COOKIE_EXPIRATION = 7776000; //90일
    private final UsersRepository usersRepository;
    private final TermsRepository termsRepository;
    private final UserTermsRepository userTermsRepository;
    private final AuthService authService;

    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;
    BadWordFiltering badWordFiltering = new BadWordFiltering();

    //스프링시큐리티 BCryptPasswordEncoder 기본 round = 10으로 설정되어 있음
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    public UserService(UsersRepository usersRepository, TermsRepository termsRepository, UserTermsRepository userTermsRepository, AuthService authService, RedisUtil redisUtil, JwtUtil jwtUtil, JavaMailSender emailSender, SpringTemplateEngine templateEngine) {
        this.usersRepository = usersRepository;
        this.termsRepository = termsRepository;
        this.userTermsRepository = userTermsRepository;
        this.authService = authService;
        this.redisUtil = redisUtil;
        this.jwtUtil = jwtUtil;
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    @Transactional
    public ResponseEntity signup(SignupDto signUp,HttpServletResponse response) {
        final SignupRes signupRes = new SignupRes();

        // 데이터베이스 저장 중에 발생할 수 있는 에러를 처리
        try {
            // 이름 비속어, 욕설 필터 에러
            if(badWordFiltering.blankCheck(signUp.getName())){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, "비속어, 욕설은 사용 불가합니다."),
                        HttpStatus.BAD_REQUEST);
            }
            // Users 객체 생성 및 저장
            Users user = signUp.toUser();
            Users signupUser = usersRepository.save(user);
            System.out.println(TransactionSynchronizationManager.getCurrentTransactionName());

            // UserTerms 객체 생성 및 저장
            List<UserTerms> userTermsList = new ArrayList<>();
            for(TermsDto termDto : signUp.getTerms()){
                UserTerms userTerm = new UserTerms();
                userTerm.setUsers(signupUser);
                Optional<Terms> terms = termsRepository.findById((long)termDto.getTermId());
                // optional 확인 & 유효성 에러 처리
                if (!terms.isPresent()) {
                    return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, ResponseMessage.BAD_REQUEST_TERMS), HttpStatus.BAD_REQUEST);
                }
                // 필수 약관 동의 에러
                if(terms.get().getIsRequired()==true && Boolean.parseBoolean(termDto.getIsAgree())==false){
                    return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,"필수 약관에 동의해주세요"),HttpStatus.BAD_REQUEST);
                }
                userTerm.setTerms(terms.get());
                userTerm.setIsAgree(Boolean.parseBoolean(termDto.getIsAgree()));
                userTermsList.add(userTerm);
                System.out.println(userTermsList);
            }

            userTermsRepository.saveAll(userTermsList);
            System.out.println(TransactionSynchronizationManager.getCurrentTransactionName());

            //jwt 생성 - jwt에 userName,userID 넣어서 생성
            TokenDto tokenDto = authService.generateToken(SERVER,signupUser);

            // RT 저장
            HttpCookie httpCookie = ResponseCookie.from("refresh-token", tokenDto.getRefreshToken())
                    .maxAge(COOKIE_EXPIRATION)
                    .httpOnly(true)
                    .secure(true)
                    .build();

            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDto.getAccessToken());
            response.setHeader(HttpHeaders.SET_COOKIE, httpCookie.toString());

            signupRes.setLoginType(signupUser.getLoginType());
            return new ResponseEntity(DefaultRes.res(StatusCode.CREATED, ResponseMessage.CREATED_USER,tokenDto), HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity login(LoginDto loginDto,HttpServletResponse response){
        final SignupRes signinRes = new SignupRes();
        try{
            //해당 이메일 존재하는지 DB에서 찾기
            Optional<Users> user = usersRepository.findByEmail(loginDto.getEmail());
            // 이메일 존재하지 않는경우도 에러처리
            if(!user.isPresent()){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,ResponseMessage.LOGIN_FAIL),HttpStatus.BAD_REQUEST);
            }

            // 암호화된 password 동일한지 비교
            if(!passwordEncoder.matches(loginDto.getPassword(),user.get().getPassword())){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,ResponseMessage.LOGIN_FAIL),HttpStatus.BAD_REQUEST);
            }
            //jwt 생성 - user도 넣어서 만듦
            // accessToken, refreshToken 생성 -> accessToken은 Header에 바로 전달
            TokenDto tokenDto = authService.generateToken(SERVER,user.get());
            // refreshToken은 redis에 저장 (key : userID, value: refreshToken, 2주동안)
            redisUtil.setDataExpire(String.valueOf(user.get().getEmail()),tokenDto.getRefreshToken(), 14L * 24L * 60L * 60L); // 2주
            // RT 저장
            HttpCookie httpCookie = ResponseCookie.from("refresh-token", tokenDto.getRefreshToken())
                    .maxAge(COOKIE_EXPIRATION)
                    .httpOnly(true)
                    .secure(true)
                    .build();

            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDto.getAccessToken());
            response.setHeader(HttpHeaders.SET_COOKIE, httpCookie.toString());

            signinRes.setLoginType(user.get().getLoginType());

            return new ResponseEntity(DefaultRes.res(StatusCode.OK,ResponseMessage.LOGIN_SUCCESS,tokenDto),HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 로그아웃
    @Transactional
    public ResponseEntity logout(String accessToken){
        try{
            // 해당 Access Token 유효시간 가지고 와서 BlackList 로 저장하기
            redisUtil.setBlackList(accessToken,"logout",jwtUtil.getRemainingTimeUntilExpiration(accessToken,secretKey));
            return new ResponseEntity(DefaultRes.res(StatusCode.OK,ResponseMessage.LOGOUT_SUCCESS),HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity findPwd(PasswordDto PasswordDto){
        try{
            Optional<Users> user = usersRepository.findByEmail(PasswordDto.getEmail());
            // 이메일 존재하지 않는경우 에러처리
            if(!user.isPresent()){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,"해당 유저가 존재하지 않습니다."),HttpStatus.BAD_REQUEST);
            }
            // 1. 새로운 비밀번호(bcrypt 암호화)가 이전의 비밀번호와 일치하지 않는지 확인
            DefaultRes<Boolean> responseBody = (DefaultRes<Boolean>) verifyPwd(user.get().getEmail(),PasswordDto.getNewPassword()).getBody();
            Boolean data = responseBody.getData();
//            if(passwordEncoder.matches(PasswordDto.getNewPassword(),user.get().getPassword())){
//                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,"이전의 비밀번호와 일치합니다."),HttpStatus.BAD_REQUEST);
//            }
            if(data){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,"이전의 비밀번호와 일치합니다."),HttpStatus.BAD_REQUEST);
            }
            // 2. 새로운 비밀번호 저장
            // Spring Data Jpa 는 기본적으로 JPA 영속성 컨텍스트 유지를 제공한다.
            // 이 상태에서 해당 데이터의 값을 변경하면 자동으로 변경사항이 DB에 반영한다.
            // 즉, 별도로 Update 쿼리를 날리지 않아도, 데이터만 변경하면 알아서 변경
            user.get().setPassword(passwordEncoder.encode(PasswordDto.getNewPassword()));
            return new ResponseEntity(DefaultRes.res(StatusCode.OK,ResponseMessage.FIND_PASSWORD),HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 회원정보 조회
    @Transactional
    public ResponseEntity readUser(String userEmail){
        try{
            Optional<Users> user = usersRepository.findByEmail(userEmail);
            if(!user.isPresent()){
                return new ResponseEntity(DefaultRes.res(StatusCode.UNAUTHORIZED, ResponseMessage.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity(DefaultRes.res(StatusCode.OK,ResponseMessage.READ_USER,user),HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 회원정보 수정
    @Transactional
    public ResponseEntity updateUser(String userEmail,updateUserDto updateUserDto){
        try{
            Optional<Users> user = usersRepository.findByEmail(userEmail);
            if(!user.isPresent()){
                return new ResponseEntity(DefaultRes.res(StatusCode.UNAUTHORIZED, ResponseMessage.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
            }
            if(updateUserDto.getName() == ""){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,"이름을 입력해주세요"),HttpStatus.BAD_REQUEST);
            }
            System.out.println(updateUserDto);
            user.get().setName(updateUserDto.getName());
            user.get().setBirth(updateUserDto.getBirth());
            user.get().setGender(updateUserDto.getGender());
            return new ResponseEntity(DefaultRes.res(StatusCode.OK,ResponseMessage.UPDATE_USER),HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 비밀번호 변경
    @Transactional
    public ResponseEntity changePwd(String userEmail, String newPassword){
        try{
            Optional<Users> user = usersRepository.findByEmail(userEmail);
            // 이메일 존재하지 않는경우 에러처리
            if(!user.isPresent()) {
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, "해당 유저가 존재하지 않습니다."), HttpStatus.BAD_REQUEST);
            }
            // 1. 입력한 새로운 비밀번호가 이전의 비밀번호와 일치하지 않는지 확인
            DefaultRes<Boolean> responseBody = (DefaultRes<Boolean>) verifyPwd(userEmail,newPassword).getBody();
            Boolean data = responseBody.getData();
            if(data){
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,"이전의 비밀번호와 일치합니다."),HttpStatus.BAD_REQUEST);
            }
            // 3. 새로운 비밀번호 저장
            // Spring Data Jpa 는 기본적으로 JPA 영속성 컨텍스트 유지를 제공한다.
            // 이 상태에서 해당 데이터의 값을 변경하면 자동으로 변경사항이 DB에 반영한다.
            // 즉, 별도로 Update 쿼리를 날리지 않아도, 데이터만 변경하면 알아서 변경
            user.get().setPassword(passwordEncoder.encode(newPassword));
            return new ResponseEntity(DefaultRes.res(StatusCode.OK,ResponseMessage.CHANGE_PASSWORD),HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 이전의 비밀번호 일치 여부 확인
    @Transactional
    public ResponseEntity verifyPwd(String userEmail, String password){
        try{
            Optional<Users> user = usersRepository.findByEmail(userEmail);
            // 이메일 존재하지 않는경우 에러처리
            if(!user.isPresent()) {
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, "해당 유저가 존재하지 않습니다."), HttpStatus.BAD_REQUEST);
            }
            // 1. 입력한 비밀번호와 현재 DB의 비밀번호가 일치하는지 확인
            if(passwordEncoder.matches(password,user.get().getPassword())){
                return new ResponseEntity(DefaultRes.res(StatusCode.OK,"이전 비밀번호와 일치합니다.",true),HttpStatus.OK);
            }
            return new ResponseEntity(DefaultRes.res(StatusCode.OK,"이전 비밀번호와 일치하지 않습니다.",false),HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

     // 회원탈퇴
    @Transactional
    public ResponseEntity WithdrawalUser(String accessToken,String userEmail){
        try{
            Optional<Users> user = usersRepository.findByEmail(userEmail);
            if(!user.isPresent()){
                return new ResponseEntity(DefaultRes.res(StatusCode.UNAUTHORIZED, ResponseMessage.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
            }
            usersRepository.delete(user.get());

            // 해당 Access Token 유효시간 가지고 와서 BlackList 로 저장하기
            redisUtil.setBlackList(accessToken,"logout",jwtUtil.getRemainingTimeUntilExpiration(accessToken,secretKey));
            return new ResponseEntity(DefaultRes.res(StatusCode.OK,ResponseMessage.WITHDRAWAL_USER),HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity(DefaultRes.res(StatusCode.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 문의하기
    @Transactional
    public ResponseEntity sendEmailMessage(InquiryDto inquiryDto,String userName) throws Exception {
        MimeMessage message = createMessage(inquiryDto,userName);
        emailSender.send(message);
        return new ResponseEntity(DefaultRes.res(StatusCode.OK, "문의하기 성공"), HttpStatus.OK);
    }

    private MimeMessage createMessage(InquiryDto inquiryDto,String userName) throws Exception {
        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, onedoitEmail); // 보낼 이메일 설정
        message.setSubject("문의사항 요청 -" + inquiryDto.getEmail()); // 이메일 제목
        message.setText(setContext(inquiryDto,userName), "utf-8", "html"); // 내용 설정(Template Process)
        return message;
    }

    private String setContext(InquiryDto inquiryDto,String userName) { // 타임리프 설정하는 코드
        Context context = new Context();
        context.setVariable("name", userName); // Template에 전달할 데이터 설정
        context.setVariable("email", inquiryDto.getEmail());
        context.setVariable("content", inquiryDto.getContent());
        context.setVariable("userAgent", inquiryDto.getUserAgent());
        return templateEngine.process("email_contact_form", context);
    }

    // 카카오 로그인(카카오 유저 정보 받아오는거 까지의 기능)
    @Transactional
    public ResponseEntity kakaoLoginService(String code,HttpServletResponse response) throws JsonProcessingException { //데이터를 리턴해주는 컨트롤러 함수(@ResponseBody)
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);

        // 2. 토큰으로 카카오 API 호출
        SocialUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);
        System.out.println(kakaoUserInfo);

        //3. 이미 가입된 유저인지 아닌지 확인
        Users users = usersRepository.findByEmail(kakaoUserInfo.getEmail())
                .orElse(null);
        // 이미 가입된 경우
        if(users != null){
            if(users.getLoginType() != LoginType.KAKAO){ //카카오 유저가 아닌경우 에러처리
                return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST,users.getLoginType()+"로 가입된 유저입니다."),HttpStatus.BAD_REQUEST);
            }
            //jwt 생성 - jwt에 userName,userID 넣어서 생성
            TokenDto tokenDto = authService.generateToken(SERVER,users);

            // RT 저장
            HttpCookie httpCookie = ResponseCookie.from("refresh-token", tokenDto.getRefreshToken())
                    .maxAge(COOKIE_EXPIRATION)
                    .httpOnly(true)
                    .secure(true)
                    .build();

            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDto.getAccessToken());
            response.setHeader(HttpHeaders.SET_COOKIE, httpCookie.toString());

            return new ResponseEntity(DefaultRes.res(StatusCode.OK, ResponseMessage.KAKAO_LOGIN_SUCCESS,tokenDto), HttpStatus.OK);
        }else{ // 회원가입 필요
            return new ResponseEntity(DefaultRes.res(StatusCode.CREATED, "카카오 회원가입 필요",kakaoUserInfo), HttpStatus.CREATED);
        }
    }

    //1. "인가 코드"로 "액세스 토큰" 요청
    private String getAccessToken(String code){
        //post 방식으로 key-value 타입으로 데이터 요청(카카오톡쪽으로)
        RestTemplate rt = new RestTemplate(); //http 요청 편하게 가능

        //HttpHeader 오브젝트 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type","application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("grant_type","authorization_code");
        params.add("client_id","19cc36f3dd9d3c88e8ab9d797fa5d79e");
        params.add("redirect_uri","https://main.dn2ba8ub4gfi8.amplifyapp.com/api/oauth/kakao/callback");
        params.add("code",code);

        // HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        // => 이유 : 밑의 exchange에 매개변수 값이 HttpEntity여서 한번에 담아줌
        HttpEntity<MultiValueMap<String,String>> kakaoTokenRequest = new HttpEntity<>(params,headers); //위의 param과 header 값 가진 엔티티

        //Http 요청하기 - Post 방식으로 - Response 변수의 응답 받음
        ResponseEntity<String> response = rt.postForEntity(
                "https://kauth.kakao.com/oauth/token",
                kakaoTokenRequest,String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        try {
            String responseBody = response.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            // System.out.println(jsonNode.get("access_token").asText());
            return jsonNode.get("access_token").asText();

        } catch (JsonProcessingException e) {
            // JsonProcessingException 처리
            e.printStackTrace(); // 또는 예외 처리 방식에 맞게 처리
            return null; // 예외 발생 시 기본값인 null 반환
        } catch (Exception e) {
            // 그 외의 예외 처리
            e.printStackTrace(); // 또는 예외 처리 방식에 맞게 처리
            return null; // 예외 발생 시 기본값인 null 반환
        }
    }

    // 2. 토큰으로 카카오 API 호출
    private SocialUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException{
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );
        System.out.println(response);

        // responseBody에 있는 정보를 꺼냄
        String responseBody = response.getBody();
        System.out.println(responseBody);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        String email = jsonNode.get("kakao_account").get("email").asText();
        String gender = String.valueOf(jsonNode.get("kakao_account").get("gender"));

        System.out.println(email);
        System.out.println(gender);

        return new SocialUserInfoDto(email,gender);
    }

}
