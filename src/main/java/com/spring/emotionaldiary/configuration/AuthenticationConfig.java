package com.spring.emotionaldiary.configuration;

import com.spring.emotionaldiary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor //생성자 주입 어노테이션 , 따로 생성자 주입및 @Autowired 사용 필요X
public class AuthenticationConfig {

    private final UserService userService;
    @Value("${jwt.secret}")
    private String secretKey;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity
                .httpBasic().disable() //인증 방식을 ui가 아닌 토큰 인증방식 채택, 스프링시큐리티에서 만들어주는 로그인 페이지를 안쓰기 위해
                .csrf().disable() //프론트엔드가 분리된 환경을 가정
                .cors().and()
                .authorizeRequests()
                .antMatchers("/api/v1/users","/api/v1/users/login","/api/v1/users/email-validation","/auth/kakao/callback").permitAll() //접근 허용하는 url
                .anyRequest().authenticated() //나머지는 다 인증처리 필요
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //jwt 사용하는 경우에 씀, 세션 사용 안함
                .and()
                //jwt 설정하는 경우, addFilter를 통해 직접 설정해줘야함
                .addFilterBefore(new JwtFilter(userService,secretKey), UsernamePasswordAuthenticationFilter.class)
                // username과 passoword를 가지고 인증하기 전에 jwt로 인증을하기 때문에 앞에 JwtFilter 설정
                .build();
    }
}
