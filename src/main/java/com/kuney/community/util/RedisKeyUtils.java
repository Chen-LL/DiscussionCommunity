package com.kuney.community.util;

/**
 * @author kuneychen
 * @since 2022/6/19 21:47
 */
public class RedisKeyUtils {

    private final static String DELIMITER = ":";
    private final static String LIKE_ENTITY_PREFIX = "like:entity:";
    private final static String LIKE_USER_PREFIX = "like:user:";
    private final static String FOLLOWEE_PREFIX = "followee:";
    private final static String FOLLOWER_PREFIX = "follower:";

    /**
     *
     * @param entityType
     * @param entityId
     * @return like:entity:entityType:entityId -> set(userId)
     */
    public static String getLikeEntityKey(int entityType, int entityId) {
        return LIKE_ENTITY_PREFIX + entityType + DELIMITER + entityId;
    }

    /**
     *
     * @param userId
     * @return like:user:userId -> int
     */
    public static String userLikeCountKey(int userId) {
        return LIKE_USER_PREFIX + userId;
    }

    /**
     *
     * @param userId
     * @param entityType
     * @return followee:userId:entityType -> zset(followee_id, now)
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return FOLLOWEE_PREFIX + userId + DELIMITER + entityType;
    }

    /**
     *
     * @param entityType
     * @param entityId
     * @return follower:entityType:entityId -> zset(follower_id, now)
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return FOLLOWER_PREFIX + entityType + DELIMITER + entityId;
    }
}
