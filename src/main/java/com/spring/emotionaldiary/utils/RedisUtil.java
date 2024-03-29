package com.spring.emotionaldiary.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class RedisUtil {
    private final StringRedisTemplate redisTemplate;
    private final StringRedisTemplate redisBlackListTemplate;

    // key를 통해 value 리턴
    public String getData(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public void setData(String key, String value) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value);
    }

    public boolean existData(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 유효 시간 동안 (key, value) 저장
    public void setDataExpire(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    // 삭제
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    public void setBlackList(String key, String value,long duration){
        ValueOperations<String, String> valueOperations = redisBlackListTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    public String getBlackList(String key) {
        ValueOperations<String, String> valueOperations = redisBlackListTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public void deleteBlackList(String key){
        redisBlackListTemplate.delete(key);
    }

    public boolean hasKeyBlackList(String key){
        return Boolean.TRUE.equals(redisBlackListTemplate.hasKey(key));
    }

}
