package com.spring.emotionaldiary.service;

import com.spring.emotionaldiary.dto.LoginDto;
import com.spring.emotionaldiary.dto.TokenDto;
import com.spring.emotionaldiary.model.Users;
import com.spring.emotionaldiary.repository.UsersRepository;
import com.spring.emotionaldiary.utils.JwtUtil;
import com.spring.emotionaldiary.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;
    private final String SERVER = "Server";
    @Value("${jwt.secret}")
    private String secretKey;

    // AT가 만료일자만 초과한 유효한 토큰인지 검사
    public boolean validate(String requestAccessTokenInHeader) {
        String requestAccessToken = resolveToken(requestAccessTokenInHeader);
        if(jwtUtil.validateAccessToken(requestAccessToken)){ //로그아웃했는 유저인지 확인
            return jwtUtil.validateAccessTokenOnlyExpired(requestAccessToken); // true = 재발급
        }
        return false;
    }

    // 토큰 재발급: validate 메서드가 true 반환할 때만 사용 -> AT, RT 재발급
    @Transactional
    public TokenDto reissue(String userEmail, String requestAccessTokenInHeader, String requestRefreshToken) {
        String requestAccessToken = resolveToken(requestAccessTokenInHeader);

        // String userName = jwtUtil.getUserName(requestAccessToken,secretKey);
        // String userEmail = jwtUtil.getUserEmail(requestAccessToken,secretKey);
        // Authentication authentication = jwtTokenProvider.getAuthentication(requestAccessToken);

        Optional<Users> users = usersRepository.findByEmail(userEmail);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(users.get().getName(), null, Collections.singleton(new SimpleGrantedAuthority("USER")));
        authenticationToken.setDetails(users.get().getEmail());

//        String principal = getPrincipal(requestAccessToken);

        String refreshTokenInRedis = redisUtil.getData("RT(" + SERVER + "):" + users.get().getEmail());
        if (refreshTokenInRedis == null) { // Redis에 저장되어 있는 RT가 없을 경우
            return null; // -> 재로그인 요청
        }

        // 요청된 RT의 유효성 검사 & Redis에 저장되어 있는 RT와 같은지 비교
        if(!jwtUtil.validateRefreshToken(requestRefreshToken) || !refreshTokenInRedis.equals(requestRefreshToken)) {
            redisUtil.deleteData("RT(" + SERVER + "):" + users.get().getEmail()); // 탈취 가능성 -> 삭제
            return null; // -> 재로그인 요청
        }

        // token이 인증된 상태를 유지하도록 context(맥락)을 유지해줌
        SecurityContextHolder.getContext().setAuthentication(authenticationToken); //토큰 넣어줌
//        String authorities = getAuthorities(authentication);

        // 토큰 재발급 및 Redis 업데이트
        redisUtil.deleteData("RT(" + SERVER + "):" + users.get().getEmail()); // 기존 RT 삭제
        TokenDto tokenDto = jwtUtil.createJwt(users.get());
        saveRefreshToken(SERVER, users.get().getEmail(), tokenDto.getRefreshToken());
        return tokenDto;
    }

    // 토큰 발급
    @Transactional
    public TokenDto generateToken(String provider, Users users) {
        // RT가 이미 있을 경우
        if(redisUtil.getData("RT(" + provider + "):" + users.getEmail()) != null) {
            redisUtil.deleteData("RT(" + provider + "):" + users.getEmail()); // 삭제
        }

        // AT, RT 생성 및 Redis에 RT 저장
        TokenDto tokenDto = jwtUtil.createJwt(users);
        saveRefreshToken(provider, users.getEmail(), tokenDto.getRefreshToken());
        return tokenDto;
    }

    // RT를 Redis에 저장
    @Transactional
    public void saveRefreshToken(String provider, String principal, String refreshToken) {
        redisUtil.setDataExpire("RT(" + provider + "):" + principal, // key
                refreshToken, // value
                14L * 24L * 60L * 60L); // timeout(milliseconds), 2주
    }

    // "Bearer {AT}"에서 {AT} 추출
    public String resolveToken(String requestAccessTokenInHeader) {
        if (requestAccessTokenInHeader != null && requestAccessTokenInHeader.startsWith("Bearer ")) {
            return requestAccessTokenInHeader.substring(7);
        }
        return null;
    }
}
