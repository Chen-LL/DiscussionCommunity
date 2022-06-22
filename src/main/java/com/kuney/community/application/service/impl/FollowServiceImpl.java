package com.kuney.community.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.service.FollowService;
import com.kuney.community.application.service.UserService;
import com.kuney.community.util.*;
import com.kuney.community.util.Constants.EntityType;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * @author kuneychen
 * @since 2022/6/20 21:34
 */
@Service
@AllArgsConstructor
public class FollowServiceImpl implements FollowService {

    private RedisTemplate redisTemplate;
    private UserService userService;
    private HostHolder hostHolder;

    @Override
    public void follow(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtils.getFolloweeKey(userId, entityType);
        String followerKey = RedisKeyUtils.getFollowerKey(EntityType.USER, entityId);
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    @Override
    public void unFollow(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtils.getFolloweeKey(userId, entityType);
        String followerKey = RedisKeyUtils.getFollowerKey(EntityType.USER, entityId);
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }

    @Override
    public long followeeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtils.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    @Override
    public long followerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtils.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    @Override
    public boolean isFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtils.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    @Override
    public Page<Map<String, Object>> followeePage(int userId, int pageNum) {
        String followeeKey = RedisKeyUtils.getFolloweeKey(userId, EntityType.USER);
        return getPage(pageNum, followeeKey);
    }

    @Override
    public Page<Map<String, Object>> followerPage(int userId, int pageNum) {
        String followerKey = RedisKeyUtils.getFollowerKey(EntityType.USER, userId);
        return getPage(pageNum, followerKey);
    }

    private Page<Map<String, Object>> getPage(int pageNum, String followeeKey) {
        int start = (pageNum - 1) * Constants.PAGE_SIZE;
        int end = start + Constants.PAGE_SIZE - 1;
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, start, end);
        if (ObjCheckUtils.isEmpty(targetIds)) {
            return PageUtils.handle(pageNum, Constants.PAGE_SIZE, 0, null);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        User loginUser = hostHolder.getUser();
        for (Integer targetId : targetIds) {
            HashMap<String, Object> item = new HashMap<>();
            User user = userService.getUser(targetId);
            item.put("user", user);

            Double time = redisTemplate.opsForZSet().score(followeeKey, targetId);
            LocalDateTime followTime = Instant.ofEpochMilli(time.longValue()).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
            item.put("followTime", followTime);

            if (loginUser != null) {
                item.put("isFollowed", isFollowed(loginUser.getId(), EntityType.USER, targetId));
            }

            list.add(item);
        }
        Long total = redisTemplate.opsForZSet().zCard(followeeKey);
        return PageUtils.handle(pageNum, Constants.PAGE_SIZE, total, list);
    }


}
