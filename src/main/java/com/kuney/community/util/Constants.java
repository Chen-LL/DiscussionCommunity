package com.kuney.community.util;

/**
 * @author kuneychen
 * @since 2022/6/10 15:16
 */
public interface Constants {
    int PAGE_NUM = 1;
    int PAGE_SIZE = 10;

    /**
     * 账号激活状态码
     */
    interface Activation {
        int SUCCESS = 0;
        int REPEAT = 1; //重复激活
        int FAIL = 2; // 失败，激活码错误
    }

    /**
     * 用户注册状态码
     */
    interface Register {
        int SUCCESS = 0;
        int USERNAME_EXIST = 1; // 用户名已存在
        int EMAIL_REGISTERED = 2; // 邮箱已被注册
    }

    interface Login {
        // 登录错误码
        int USERNAME_ERROR = 0;
        int PASSWORD_ERROR = 1;
        int CODE_ERROR = 2;
        int NOT_ACTIVE = 3;
        // 登录凭证过期时间
        int DEFAULT_EXPIRE_SECONDS = 60 * 60 * 12; // 12小时
        int REMEMBER_EXPIRE_SECONDS = 60 * 60 * 24 * 7; // 一周
    }

    /**
     * 文件上传位置
     */
    interface Location {
        String LOCAL = "local";
        String REMOTE = "remote";
    }

    /**
     * 实体类型
     */
    interface EntityType {
        int POST = 1; // 帖子
        int COMMENT = 2; // 评论
    }
}
