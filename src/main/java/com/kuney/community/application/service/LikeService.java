package com.kuney.community.application.service;

/**
 * @author kuneychen
 * @since 2022/6/19 22:08
 */
public interface LikeService {
    void like(int entityType, int entityId, int userId, int toUserId);

    long likeCount(int entityType, int entityId);

    int getLikeStatus(int entityType, int entityId, int userId);

    int getUserLikeCount(int userId);
}
