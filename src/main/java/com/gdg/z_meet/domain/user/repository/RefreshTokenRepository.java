package com.gdg.z_meet.domain.user.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RefreshTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RefreshTokenRepository(@Qualifier("matchingRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String token, Long userId, long ttl) {
        redisTemplate.opsForValue().set(token, userId.toString(), ttl, TimeUnit.SECONDS);
    }

    public String findByToken(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    public void delete(String token) {
        redisTemplate.delete(token);
    }
}
