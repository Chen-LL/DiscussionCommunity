package com.kuney.community.application.service;

import com.kuney.community.application.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param user
     * @return 0-执行成功，1-账号已注册，2-邮箱已注册
     */
    int userRegister(User user);

    /**
     * 用户激活
     * @param userId
     * @param activationCode 激活码
     * @return 0-激活成功，1-账号已激活，2-激活失败
     */
    int userActivation(Integer userId, String activationCode);

    /**
     * 用户登录
     * @param username 账号
     * @param password 密码
     * @param expireSeconds ticket过期时间
     * @return 返回登录凭证ticket，以及resultCode
     */
    Map<String, Object> userLogin(String username, String password, int expireSeconds);

    void userLogout(String ticket);
}
