package com.yelp_like.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Snowflake-like id generator backed by Redis incr.
 */
@Component
@RequiredArgsConstructor
public class RedisIdWorker {

    private static final long BEGIN_TIMESTAMP = 1672531200L; // 2023-01-01
    private static final int COUNT_BITS = 32;
    private final StringRedisTemplate stringRedisTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd");

    public long nextId(String keyPrefix) {
        LocalDateTime now = LocalDateTime.now();
        long timestamp = now.toEpochSecond(ZoneOffset.UTC) - BEGIN_TIMESTAMP;
        String date = now.format(DATE_FORMATTER);
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        return (timestamp << COUNT_BITS) | count;
    }
}

