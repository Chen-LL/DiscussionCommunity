package com.kuney.community.util;

/**
 * @author kuneychen
 * @since 2022/6/10 15:16
 */
public interface Constants {
    int PAGE_NUM = 1;
    int PAGE_SIZE = 10;

    interface UserActivation {
        int SUCCESS = 0;
        int REPEAT = 1;
        int FAIL = 2;
    }

    interface Login {
        int USERNAME_ERROR = 0;
        int PASSWORD_ERROR = 1;
        int CODE_ERROR = 2;
        int DEFAULT_EXPIRE_SECONDS = 60 * 60 * 12; // 12小时
        int REMEMBER_EXPIRE_SECONDS = 60 * 60 * 24 * 7; // 一周
    }

    interface Location {
        String LOCAL = "local";
        String REMOTE = "remote";
    }
}
