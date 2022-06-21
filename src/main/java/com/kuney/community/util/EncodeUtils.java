package com.kuney.community.util;

import org.springframework.util.DigestUtils;

import java.util.Base64;
import java.util.UUID;

/**
 * @author kuneychen
 * @since 2022/6/12 1:28
 */
public class EncodeUtils {

    /**
     * 密码加密
     *
     * @param password 原始密码
     * @param salt     盐值
     * @return
     */
    public static String encodePassword(String password, String salt) {
        return DigestUtils.md5DigestAsHex((password + salt).getBytes());
    }

    /**
     * 生成随机盐值、激活码
     *
     * @return
     */
    public static String generateCode() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6);
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 生成登录凭证
     *
     * @param userId 用户id
     * @return (用户id的base64编码 + 当前时间戳)的md5编码
     */
    public static String generateTicket(int userId) {
        String key = Base64.getEncoder().encodeToString(String.valueOf(userId).getBytes()) + System.currentTimeMillis();
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
