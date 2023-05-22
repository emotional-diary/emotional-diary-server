package com.spring.emotionaldiary.configuration;

import com.spring.emotionaldiary.service.UserService;
import com.spring.emotionaldiary.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j //Simple Logging Facade for Java, 로깅 찍어볼 수 있음!
public class JwtFilter extends OncePerRequestFilter { //안보내는 요청에도 허용할 수 있기 때문에 매번 jwt 토큰 요청해야함

    private final UserService userService;
    @Value("${jwt.secret}")
    private final String secretKey;

    //인증을 통과하는 문이라고 생각, 권한 인증과정
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //header에서 Bearer 토큰 꺼내기
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("authorization: {}",authorization);

//        if ("/api/v1/users".equals(request.getRequestURI()) || "/api/v1/users/login".equals(request.getRequestURI())) {
//            filterChain.doFilter(request, response);
//            return;
//        }
        //토큰 안보내면 blcok
        //권한 처리하기 전에 토큰 없을경우 에러 처리
        if(authorization==null || !authorization.startsWith("Bearer ")){
            log.error("authorization을 잘못보냈습니다.");
            filterChain.doFilter(request,response);
            return;
        }

        //토큰 꺼내기(Bearer뒤의 token)
        String token = authorization.split(" ")[1];
        log.info(token);

        //Token이 Expired가 되었는지 여부 체크
        if(JwtUtil.isExpired(token,secretKey)){
            log.error("토큰이 만료되었습니다.");
            filterChain.doFilter(request,response);
            return;
        }

        //Email 토큰에서 꺼내기
        String userName=JwtUtil.getUserName(token,secretKey);
        log.info(userName);

        //권한 부여, SimpleGrantedAuthority : db에 role 지정했으면 안에서 지정해서 사용가능
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userName,null, List.of(new SimpleGrantedAuthority("USER")));

        //Detail 넣어줌

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); //토큰에다가 요청값 detail추가
        // token이 인증된 상태를 유지하도록 context(맥락)을 유지해줌
        SecurityContextHolder.getContext().setAuthentication(authenticationToken); //토큰 넣어줌
        filterChain.doFilter(request,response); //filter 체인에 request 넘겨주면 인증되었다는 도장 찍 -> 통과
    }
}
