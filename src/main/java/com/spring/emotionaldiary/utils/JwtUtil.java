package com.spring.emotionaldiary.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    //userName 토큰에서 꺼내오기
    public static String getUserName(String token, String secretKey){
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().get("userName",String.class);
    }

    public static boolean isExpired(String token, String secretKey){
        // jwt 토큰 만료된 시간이 현재시간보다 이전이면 expired(만료)된것으로 처리
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().getExpiration().before(new Date());
    }

    //jwt는 사용자가 원하는 정보를 담을 수 있는 공간을 제공함 -> email로 사용
    public static String createJwt(String userName, String secretKey, Long expiredMs,HttpServletResponse response){ //토큰 생성 : 토큰에서 꺼내올 값 매개변수로 설정
        Claims claims = Jwts.claims(); //일종의 맵, 여기다가 email 저장 가능
        claims.put("userName",userName);

        String jwt = Jwts.builder()
                .setClaims(claims) //userName이 클레임이라는 공간안에 들어감
                .setIssuedAt(new Date(System.currentTimeMillis())) //현재 시간 넣어줘야함
                .setExpiration(new Date(System.currentTimeMillis()+expiredMs))
                .signWith(SignatureAlgorithm.HS256, secretKey) //HS256 알고리즘 많이 사용
                .compact();

        //Http Header에 jwt 등록
        // response.setHeader("Authorization", "Bearer " + jwt);

        //jwt를 쿠키에 저장
        ResponseCookie cookie = ResponseCookie.from("jwt",jwt)
                .maxAge(7 * 24 * 60 * 60)
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .build();
        //http Header에 쿠키 저장
        response.setHeader("Set-Cookie", cookie.toString());

        return cookie.toString();
    }
}