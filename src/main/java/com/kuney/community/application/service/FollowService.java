package com.kuney.community.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

/**
 * @author kuneychen
 * @since 2022/6/20 21:34
 */
public interface FollowService {
    void follow(int userId, int entityType, int entityId);

    void unFollow(int userId, int entityType, int entityId);

    long followeeCount(int userId, int entityType);

    long followerCount(int entityType, int entityId);

    boolean isFollowed(int userId, int entityType, int entityId);

    Page<Map<String, Object>> followeePage(int userId, int pageNum);

    Page<Map<String, Object>> followerPage(int userId, int pageNum);
}
