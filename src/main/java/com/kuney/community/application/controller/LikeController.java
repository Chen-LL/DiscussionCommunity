package com.kuney.community.application.controller;

import com.kuney.community.annotation.LoginRequired;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.service.LikeService;
import com.kuney.community.event.Event;
import com.kuney.community.event.EventProducer;
import com.kuney.community.util.Constants;
import com.kuney.community.util.Constants.EntityType;
import com.kuney.community.util.Constants.LikeStatus;
import com.kuney.community.util.HostHolder;
import com.kuney.community.util.RedisKeyUtils;
import com.kuney.community.util.Result;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kuneychen
 * @since 2022/6/19 22:05
 */
@RestController
@RequestMapping("like")
@AllArgsConstructor
public class LikeController implements Constants.KafkaTopic {

    private LikeService likeService;
    private HostHolder hostHolder;
    private EventProducer eventProducer;
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping
    @LoginRequired
    public Result likeEntity(int entityType, int entityId, int toUserId, int postId) {
        User user = hostHolder.getUser();
        likeService.like(entityType, entityId, user.getId(), toUserId);
        long likeCount = likeService.likeCount(entityType, entityId);
        int likeStatus = likeService.getLikeStatus(entityType, entityId, user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("likeCount", likeCount);
        data.put("likeStatus", likeStatus);

        if (likeStatus == LikeStatus.LIKED) {
            Event event = new Event();
            event.setTopic(LIKE);
            event.setUserId(user.getId());
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setEntityUserId(toUserId);
            event.setData("postId", postId);
            eventProducer.sendMessage(event);
        }
        if (entityType == EntityType.POST) {
            redisTemplate.opsForSet().add(RedisKeyUtils.getPostScoreKey(), postId);
        }
        return Result.data(data);
    }

}
