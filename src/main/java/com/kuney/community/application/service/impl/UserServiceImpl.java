package com.kuney.community.application.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.mapper.UserMapper;
import com.kuney.community.application.service.UserService;
import com.kuney.community.util.CommunityUtils;
import com.kuney.community.util.Constants.Activation;
import com.kuney.community.util.Constants.Login;
import com.kuney.community.util.Constants.Register;
import com.kuney.community.util.EncodeUtils;
import com.kuney.community.util.ObjCheckUtils;
import com.kuney.community.util.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${user.image.default}")
    private String headUrl;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${community.domain}")
    private String domain;

    private CommunityUtils communityUtils;
    private TemplateEngine templateEngine;
    private ThreadPoolExecutor executor;
    // private LoginTicketService loginTicketService;
    private RedisTemplate redisTemplate;

    public UserServiceImpl(CommunityUtils communityUtils, TemplateEngine templateEngine,
                           ThreadPoolExecutor executor, RedisTemplate redisTemplate) {
        this.communityUtils = communityUtils;
        this.templateEngine = templateEngine;
        this.executor = executor;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public int userRegister(User user) {
        Integer existUsername = this.lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .count();
        if (existUsername > 0) {
            return Register.USERNAME_EXIST;
        }
        Integer existEmail = this.lambdaQuery()
                .eq(User::getEmail, user.getEmail())
                .count();
        if (existEmail > 0) {
            return Register.EMAIL_REGISTERED;
        }
        user.setActivationCode(EncodeUtils.generateCode());
        user.setSalt(EncodeUtils.generateCode());
        user.setCreateTime(LocalDateTime.now());
        user.setHeaderUrl(String.format(this.headUrl, new Random().nextInt(1000)));
        user.setPassword(EncodeUtils.encodePassword(user.getPassword(), user.getSalt()));
        this.save(user);

        // 发送激活邮件
        CompletableFuture.runAsync(() -> {
            String url = String.format("%s/user/activation/%d/%s", domain + contextPath, user.getId(), user.getActivationCode());
            Context context = new Context();
            context.setVariable("username", user.getEmail());
            context.setVariable("url", url);
            String content = templateEngine.process("mail/activation", context);
            communityUtils.sendSimpleMail(user.getEmail(), "激活账号", content);
        }, executor);

        return Register.SUCCESS;
    }

    @Override
    public int userActivation(Integer userId, String activationCode) {
        User user = this.getUser(userId);
        if (ObjCheckUtils.isNull(user) || !user.getActivationCode().equals(activationCode)) {
            return Activation.FAIL;
        }
        if (user.getStatus() == 1) {
            return Activation.REPEAT;
        }
        this.lambdaUpdate().set(User::getStatus, 1).eq(User::getId, userId).update();
        return Activation.SUCCESS;
    }

    @Override
    public Map<String, Object> userLogin(String username, String password, int expireSeconds) {
        HashMap<String, Object> result = new HashMap<>(4);

        User user = this.getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (ObjCheckUtils.isNull(user)) {
            result.put("resultCode", Login.USERNAME_ERROR);
            return result;
        }
        if (user.getStatus() == 0) {
            result.put("resultCode", Login.NOT_ACTIVE);
            return result;
        }
        String encodePassword = EncodeUtils.encodePassword(password, user.getSalt());
        if (!user.getPassword().equals(encodePassword)) {
            result.put("resultCode", Login.PASSWORD_ERROR);
            return result;
        }

        /*LoginTicket ticket = new LoginTicket();
        ticket.setUserId(user.getId());
        ticket.setExpired(LocalDateTime.now().plusSeconds(expireSeconds));
        ticket.setTicket(EncodeUtils.generateTicket(ticket.getUserId()));
        loginTicketService.save(ticket);*/
        String ticket = EncodeUtils.generateTicket(user.getId());
        redisTemplate.opsForValue().set(RedisKeyUtils.getLoginTicketKey(ticket), user.getId(), expireSeconds, TimeUnit.SECONDS);
        result.put("ticket", ticket);
        return result;
    }

    @Override
    public void userLogout(String ticket) {
        // loginTicketService.lambdaUpdate()
        //         .set(LoginTicket::getStatus, 1)
        //         .eq(LoginTicket::getTicket, ticket)
        //         .update();
        redisTemplate.delete(RedisKeyUtils.getLoginTicketKey(ticket));
    }


    @Transactional
    @Override
    public void updatePassword(String newPassword, String ticket, User user) {
        newPassword = EncodeUtils.encodePassword(newPassword, user.getSalt());
        this.lambdaUpdate()
                .set(User::getPassword, newPassword)
                .eq(User::getId, user.getId())
                .update();
        this.userLogout(ticket);
        this.clearCache(user.getId());
    }

    @Override
    public User getUser(int userId) {
        User user = getCache(userId);
        if (ObjCheckUtils.isNull(user)) {
            user = initCache(userId);
        }
        return user;
    }

    private User getCache(int userId) {
        String key = RedisKeyUtils.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(key);
    }

    private User initCache(int userId) {
        User user = this.getById(userId);
        String key = RedisKeyUtils.getUserKey(userId);
        redisTemplate.opsForValue().set(key, user, 1, TimeUnit.HOURS);
        return user;
    }

    private void clearCache(int userId) {
        String key = RedisKeyUtils.getUserKey(userId);
        redisTemplate.delete(key);
    }

}
