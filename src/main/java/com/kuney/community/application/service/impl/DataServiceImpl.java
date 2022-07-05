package com.kuney.community.application.service.impl;

import com.kuney.community.application.service.DataService;
import com.kuney.community.util.RedisKeyUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * @author kuneychen
 * @since 2022/7/5 21:10
 */
@Service
@AllArgsConstructor
@Slf4j
public class DataServiceImpl implements DataService {

    private RedisTemplate<String, Object> redisTemplate;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public long getUV(LocalDate begin, LocalDate end) {
        ArrayList<String> keys = new ArrayList<>();
        LocalDate date = begin;
        while (!date.isAfter(end)) {
            keys.add(RedisKeyUtils.getUVKey(date.format(formatter)));
            date = date.plusDays(1);
        }
        String key = RedisKeyUtils.getUVKey(begin.format(formatter), end.format(formatter));
        redisTemplate.opsForHyperLogLog().union(key, keys.toArray(new String[0]));
        return redisTemplate.opsForHyperLogLog().size(key);
    }

    @Override
    public long getDAU(LocalDate begin, LocalDate end) {
        ArrayList<byte[]> keys = new ArrayList<>();
        LocalDate date = begin;
        while (!date.isAfter(end)) {
            keys.add(RedisKeyUtils.getDAUKey(date.format(formatter)).getBytes());
            date = date.plusDays(1);
        }
        String key = RedisKeyUtils.getDAUKey(begin.format(formatter), end.format(formatter));
        return redisTemplate.execute((RedisCallback<Long>) connection -> {
            connection.bitOp(RedisStringCommands.BitOperation.OR, key.getBytes(), keys.toArray(new byte[0][]));
            return connection.bitCount(key.getBytes());
        });
    }

    @Override
    public void recordUV(String ip) {
        String key = RedisKeyUtils.getUVKey(LocalDateTime.now().format(formatter));
        redisTemplate.opsForHyperLogLog().add(key, ip);
    }

    @Override
    public void recordDAU(int userId) {
        String key = RedisKeyUtils.getDAUKey(LocalDateTime.now().format(formatter));
        redisTemplate.opsForValue().setBit(key, userId, true);
    }


}
