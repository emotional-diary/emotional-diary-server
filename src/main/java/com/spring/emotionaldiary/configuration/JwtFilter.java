package com.spring.emotionaldiary.configuration;

import com.spring.emotionaldiary.service.UserService;
import com.spring.emotionaldiary.utils.JwtUtil;
import com.spring.emotionaldiary.utils.RedisUtil;
import io.jsonwebtoken.IncorrectClaimException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
@Slf4j //Simple Logging Facade for Java, 로깅 찍어볼 수 있음!
public class JwtFilter extends OncePerRequestFilter { //안보내는 요청에도 허용할 수 있기 때문에 매번 jwt 토큰 요청해야함

    private final UserService userService;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;
    @Value("${jwt.secret}")
    private final String secretKey;

    //인증을 통과하는 문이라고 생각, 권한 인증과정
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //header에서 Bearer 토큰 꺼내기
        // final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Access Token 추출
        String accessToken = resolveToken(request);
        log.info("accessToken: {}",accessToken);

        //토큰 안보내면 blcok
        //권한 처리하기 전에 토큰 없을경우 에러 처리
//        if(authorization==null || !authorization.startsWith("Bearer ")){
//            log.error("authorization을 잘못보냈습니다.");
//            filterChain.doFilter(request,response);
//            return;
//            // return new ResponseEntity(DefaultRes.res(StatusCode.UNAUTHORIZED, ResponseMessage.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
//        }

        //토큰 꺼내기(Bearer뒤의 token)
//        String token = authorization.split(" ")[1];
//        log.info(token);

        try { // 정상 토큰인지 검사
            if (accessToken != null && jwtUtil.validateAccessToken(accessToken)) {
                // 로그아웃 한 유저인지 아닌지 검사
                if (redisUtil.hasKeyBlackList(accessToken)) {
                    // RuntimeException을 강제로 발생, 예외 처리
                    throw new RuntimeException("로그아웃!");
                }
                String userName = jwtUtil.getUserName(accessToken, secretKey);
                String userEmail = jwtUtil.getUserEmail(accessToken, secretKey);
                log.info(userEmail);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userName, null, Collections.singleton(new SimpleGrantedAuthority("USER")));
                authenticationToken.setDetails(userEmail);
                // authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); //토큰에다가 요청값 detail추가

                // token이 인증된 상태를 유지하도록 context(맥락)을 유지해줌
                SecurityContextHolder.getContext().setAuthentication(authenticationToken); //토큰 넣어줌
            }
        } catch (IncorrectClaimException e) { // 잘못된 토큰일 경우
            SecurityContextHolder.clearContext();
            log.debug("Invalid JWT token.");
            response.sendError(403);
        } catch (UsernameNotFoundException e) { // 회원을 찾을 수 없을 경우
            SecurityContextHolder.clearContext();
            log.debug("Can't find user.");
            response.sendError(403);
        }

        filterChain.doFilter(request, response);

        //Token이 Expired가 되었는지 여부 체크
//        if(jwtUtil.validateAccessTokenOnlyExpired(accessToken)){
//            log.error("토큰이 만료되었습니다.");
//            filterChain.doFilter(request,response);
//            return;
//            // return new ResponseEntity(DefaultRes.res(StatusCode.UNAUTHORIZED, ResponseMessage.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
//        }

        //권한 부여, SimpleGrantedAuthority : db에 role 지정했으면 안에서 지정해서 사용가능
//        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
//                .getBody();
//
//        String userName = claims.get("userName", String.class);
//        String userID = claims.get("userID", String.class);

//        // 권한(role)을 지정할 경우에는 claim에서 읽어와서 사용
//        List<GrantedAuthority> authorities = new ArrayList<>();
//        authorities.add(new SimpleGrantedAuthority("USER"));

        // userName, userID, 권한(authorities)을 사용하여 authenticationToken 생성
//        UsernamePasswordAuthenticationToken authenticationToken =
//                new UsernamePasswordAuthenticationToken(userName, null, Collections.singleton(new SimpleGrantedAuthority("USER")));
    }

    // HTTP Request 헤더로부터 토큰 추출
    public String resolveToken(HttpServletRequest httpServletRequest) {
        String bearerToken = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

//    private UsernamePasswordAuthenticationToken getAuthenticationToken(String token, String secretKey) {
//        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
//                .getBody();
//
//        String userName = claims.get("userName", String.class);
//        String userID = claims.get("userID", String.class);
//
////        // 권한(role)을 지정할 경우에는 claim에서 읽어와서 사용
//        List<GrantedAuthority> authorities = new ArrayList<>();
//        authorities.add(new SimpleGrantedAuthority("USER"));
//
//        // userName, userID, 권한(authorities)을 사용하여 authenticationToken 생성
//        return new UsernamePasswordAuthenticationToken(userName, userID, authorities);
//    }

}
