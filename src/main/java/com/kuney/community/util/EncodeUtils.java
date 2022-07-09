package com.kuney.community.util;

import com.kuney.community.exception.CustomException;
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
        return generateSaltMD5(password, salt);
    }

    /**
     * 生成随机盐值、激活码
     *
     * @return
     */
    public static String generateCode() {
        return generateUUID().substring(0, 6);
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
    @Deprecated
    public static String generateTicket(int userId) {
        String key = Base64.getEncoder().encodeToString(String.valueOf(userId).getBytes()) + System.currentTimeMillis();
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String generateMD5(String str) {
        return DigestUtils.md5DigestAsHex(str.getBytes());
    }

    public static String generateSaltMD5(String str, String salt) {
        String base = str + salt;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    public static String generateBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public static String base64decode(String str) {
        return new String(Base64.getDecoder().decode(str));
    }

    /**
     * token由两大部分组成：
     * 第一部分：base64的用户id，可以直接解码，获取到用户id；
     * 第二部分：id+时间戳+盐的md5码再转base64；
     * <p>
     * 如何防止token被伪造：假设token泄露，想研究出token的构成，从而进行伪造。
     * <p>
     * 假设第一部分被猜到是用户id，如果想伪造其他用户的token，需要使用其他用户的id+第二部分,
     * 但是第二部分中带有原用户的id，所以伪造失败。
     * 原用户修改密码后token会刷新（利用时间戳），想伪造原用户的id，因为不知道第二部分的构成，伪造失败。
     * <p>
     * 假设想要破解第二部分，进行解码后获得的是md5码，是加盐的，即解码后的内容几乎不可能被破解，
     * 所以无法得知第二部分的构成，伪造失败。
     */
    public static String generateToken(int userId, String salt) {
        String id = String.valueOf(userId);
        String token = generateSaltMD5(id + System.currentTimeMillis(), salt);
        return generateBase64(id) + "." + generateBase64(token);
    }

    public static int parseUserId(String token) {
        if (ObjCheckUtils.isBlank(token)) {
            throw new CustomException(403, "无效的的token！");
        }
        String[] split = token.split("\\.");
        String userId = base64decode(split[0]);
        return Integer.parseInt(userId);
    }

}
