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
    private final static String LOGIN_CODE_PREFIX = "login:code:";
    private final static String LOGIN_TICKET_PREFIX = "login:ticket:";
    private final static String USER_PREFIX = "user:";
    private final static String UV_PREFIX = "uv:";
    private final static String DAU_PREFIX = "dau:";

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

    public static String getLoginCodeKey(String owner) {
        return  LOGIN_CODE_PREFIX + owner;
    }

    public static String getLoginTicketKey(String ticket) {
        return  LOGIN_TICKET_PREFIX + ticket;
    }

    public static String getUserKey(int userId) {
        return USER_PREFIX + userId;
    }

    public static String getUVKey(String date) {
        return UV_PREFIX + date;
    }

    public static String getUVKey(String begin, String end) {
        return UV_PREFIX + begin + DELIMITER + end;
    }

    public static String getDAUKey(String date) {
        return DAU_PREFIX + date;
    }

    public static String getDAUKey(String begin, String end) {
        return DAU_PREFIX + begin + DELIMITER + end;
    }
}
