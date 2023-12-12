package com.spring.emotionaldiary.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import com.spring.emotionaldiary.service.UserService;
import com.spring.emotionaldiary.utils.JwtUtil;
import com.spring.emotionaldiary.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;
import java.util.logging.ErrorManager;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor //생성자 주입 어노테이션 , 따로 생성자 주입및 @Autowired 사용 필요X
public class AuthenticationConfig {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    @Value("${jwt.secret}")
    private String secretKey;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity
                .httpBasic().disable() //인증 방식을 ui가 아닌 토큰 인증방식 채택, 스프링시큐리티에서 만들어주는 로그인 페이지를 안쓰기 위해
                .csrf().disable() //프론트엔드가 분리된 환경을 가정
                .cors().and()
                .authorizeRequests()
                .antMatchers("/api/v1/users/login/kakao","/api/v1/auth/reissue","/api/v1/auth/validate","/api/v1/users/signup","/api/v1/users/login/**","/api/v1/users/email-validation","/api/v1/users/email","/api/v1/users/find-pwd","/sample","/api/v1/users/email-validation/check").permitAll() //접근 허용하는 url
                .anyRequest().authenticated() //나머지는 다 인증처리 필요
                .and()
                .exceptionHandling()
                // 401 에러 처리
                .authenticationEntryPoint(unauthorizedEntryPoint)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //jwt 사용하는 경우에 씀, 세션 사용 안함
                .and()
                //jwt 설정하는 경우, addFilter를 통해 직접 설정해줘야함
                .addFilterBefore(new JwtFilter(userService,redisUtil,jwtUtil,secretKey), UsernamePasswordAuthenticationFilter.class)
                // username과 passoword를 가지고 인증하기 전에 jwt로 인증을하기 때문에 앞에 JwtFilter 설정
                .build();
    }

    // 401 에러 커스텀
    private final AuthenticationEntryPoint unauthorizedEntryPoint = (request, response, authException) -> {
        ResponseEntity fail = new ResponseEntity(
                DefaultRes.res(StatusCode.UNAUTHORIZED,ResponseMessage.UNAUTHORIZED),
                HttpStatus.UNAUTHORIZED
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String jsonResponse = new ObjectMapper().writeValueAsString(fail.getBody());

        PrintWriter writer = response.getWriter();
        writer.write(new String(jsonResponse.getBytes("UTF-8"), "UTF-8"));
        writer.flush();
    };
}
