package com.bandall.location_share.domain.login.jwt.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisAccessTokenBlackListRepository {
    private final RedisTemplate<String, Object> redisBlackListTemplate;

    @Value("${jwt.access-token-validity-in-seconds}")
    private Long accessTokenTimeoutTime;

    public void setBlackList(String key, Object o) {
        redisBlackListTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(o.getClass()));
        redisBlackListTemplate.opsForValue().set(key, o, accessTokenTimeoutTime * 1000, TimeUnit.MILLISECONDS);
    }

    public Object getBlackList(String key) {
        return redisBlackListTemplate.opsForValue().get(key);
    }

    public boolean deleteBlackList(String key) {
        return redisBlackListTemplate.delete(key);
    }

    public boolean isKeyBlackList(String key) {
        return redisBlackListTemplate.hasKey(key);
    }
}
