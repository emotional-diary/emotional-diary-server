package com.spring.emotionaldiary.utils;

import com.spring.emotionaldiary.dto.TokenDto;
import com.spring.emotionaldiary.model.Users;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;
    private static long accessTokenValidTime = Duration.ofMinutes(5).toMillis(); // 만료시간 5분
    private static long refreshTokenValidTime = Duration.ofDays(14).toMillis(); // 만료시간 2주

    private final RedisUtil redisUtil;

    //userName 토큰에서 꺼내오기
    public String getUserName(String token, String secretKey){
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().get("userName",String.class);
    }
    public String getUserEmail(String token, String secretKey){
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().get("userEmail",String.class);
    }

    // JWT 토큰이 만료될 때까지 남은 시간을 계산
    public Long getRemainingTimeUntilExpiration(String token, String secretKey){

        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTimeMillis = claims.getExpiration().getTime();
        System.out.println(expirationTimeMillis);
        return expirationTimeMillis - currentTimeMillis;
    }

//    public static String createAccessToken(Users users, String secretKey,HttpServletResponse response) {
//        String accessToken = createJwt(users,secretKey,accessTokenValidTime,response);
//
//        //Http Header에 jwt 등록
//        response.setHeader("Authorization", "Bearer " + accessToken);
//        return accessToken;
//    }
//
//    public static String createRefreshToken(Users users,String secretKey,HttpServletResponse response) {
//        return createJwt(users, secretKey,refreshTokenValidTime,response);
//    }

    //jwt는 사용자가 원하는 정보를 담을 수 있는 공간을 제공함 -> email로 사용
    public TokenDto createJwt(Users users){ //토큰 생성 : 토큰에서 꺼내올 값 매개변수로 설정
        Claims claims = Jwts.claims(); //일종의 맵, 여기다가 userName,userID 저장 가능
        claims.put("userName",users.getName());
        claims.put("userEmail",users.getEmail());
        Date now = new Date();

        String accessToken = Jwts.builder()
                .setClaims(claims) //userName이 클레임이라는 공간안에 들어감
                .setIssuedAt(new Date(System.currentTimeMillis())) //현재 시간 넣어줘야함
                .setExpiration(new Date(now.getTime() + accessTokenValidTime)) // 토큰 만료 시간
                .signWith(SignatureAlgorithm.HS256, secretKey) //HS256 알고리즘 많이 사용
                .setSubject("access-token")
                .compact();

        String refreshToken = Jwts.builder()
                .setIssuedAt(new Date(System.currentTimeMillis())) //현재 시간 넣어줘야함
                .setExpiration(new Date(now.getTime() + refreshTokenValidTime)) // 토큰 만료 시간
                .signWith(SignatureAlgorithm.HS256, secretKey) //HS256 알고리즘 많이 사용
                .setSubject("refresh-token")
                .compact();

        return new TokenDto(accessToken, refreshToken);
    }

    // == 토큰 검증 == //

    public boolean validateRefreshToken(String refreshToken){
        try {
            if (redisUtil.getData(refreshToken).equals("delete")) { // 회원 탈퇴했을 경우
                return false;
            }
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(refreshToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature.");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty.");
        } catch (NullPointerException e){
            log.error("JWT Token is empty.");
        }
        return false;
    }

    // Filter에서 사용
    public boolean validateAccessToken(String accessToken) {
        try {
            if (redisUtil.getData(accessToken) != null // NPE 방지
                    && redisUtil.getData(accessToken).equals("logout")) { // 로그아웃 했을 경우
                return false;
            }
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(accessToken);
            return true;
        } catch(ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 재발급 검증 API에서 사용
    public boolean validateAccessTokenOnlyExpired(String accessToken) {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(accessToken)
                    .getBody().getExpiration().before(new Date());
        } catch(ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
