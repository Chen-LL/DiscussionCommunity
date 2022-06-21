package com.kuney.community.application.service.impl;

import com.kuney.community.application.service.LikeService;
import com.kuney.community.util.Constants.LikeStatus;
import com.kuney.community.util.ObjCheckUtils;
import com.kuney.community.util.RedisKeyUtils;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @author kuneychen
 * @since 2022/6/19 22:09
 */
@Service
@AllArgsConstructor
public class LikeServiceImpl implements LikeService {

    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void like(int entityType, int entityId, int userId, int toUserId) {
        String key = RedisKeyUtils.getLikeEntityKey(entityType, entityId);
        String countKey = RedisKeyUtils.userLikeCountKey(toUserId);
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                Boolean member = operations.opsForSet().isMember(key, userId);
                operations.multi();
                if (member) {
                    operations.opsForSet().remove(key, userId);
                    operations.opsForValue().decrement(countKey);
                } else {
                    operations.opsForSet().add(key, userId);
                    operations.opsForValue().increment(countKey);
                }
                return operations.exec();
            }
        });
    }

    @Override
    public long likeCount(int entityType, int entityId) {
        String key = RedisKeyUtils.getLikeEntityKey(entityType, entityId);
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public int getLikeStatus(int entityType, int entityId, int userId) {
        String key = RedisKeyUtils.getLikeEntityKey(entityType, entityId);
        boolean member = redisTemplate.opsForSet().isMember(key, userId);
        return member ? LikeStatus.LIKED : LikeStatus.UNLIKE;
    }

    @Override
    public int getUserLikeCount(int userId) {
        String key = RedisKeyUtils.userLikeCountKey(userId);
        Object count = redisTemplate.opsForValue().get(key);
        return ObjCheckUtils.isNull(count) ? 0 : (int) count;
    }
}
