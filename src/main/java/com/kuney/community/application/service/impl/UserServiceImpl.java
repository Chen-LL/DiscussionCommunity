package com.kuney.community.application.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuney.community.application.entity.LoginTicket;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.mapper.UserMapper;
import com.kuney.community.application.service.LoginTicketService;
import com.kuney.community.application.service.UserService;
import com.kuney.community.util.CommunityUtils;
import com.kuney.community.util.Constants;
import com.kuney.community.util.EncodeUtils;
import com.kuney.community.util.ObjCheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${user.default.headUrl}")
    private String headUrl;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${community.domain}")
    private String domain;

    private CommunityUtils communityUtils;
    private TemplateEngine templateEngine;
    private ThreadPoolExecutor executor;
    private LoginTicketService loginTicketService;

    @Autowired
    public UserServiceImpl(CommunityUtils communityUtils,
                           TemplateEngine templateEngine,
                           ThreadPoolExecutor executor,
                           LoginTicketService loginTicketService) {
        this.communityUtils = communityUtils;
        this.templateEngine = templateEngine;
        this.executor = executor;
        this.loginTicketService = loginTicketService;
    }

    @Override
    public int userRegister(User user) {
        Integer existUsername = this.lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .count();
        if (existUsername > 0) {
            return 1;
        }
        Integer existEmail = this.lambdaQuery()
                .eq(User::getEmail, user.getEmail())
                .count();
        if (existEmail > 0) {
            return 2;
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

        return 0;
    }

    @Override
    public int userActivation(Integer userId, String activationCode) {
        User user = this.getById(userId);
        if (ObjCheckUtils.isNull(user) || !user.getActivationCode().equals(activationCode)) {
            return Constants.UserActivation.FAIL;
        }
        if (user.getStatus() == 1) {
            return Constants.UserActivation.REPEAT;
        }
        this.lambdaUpdate().set(User::getStatus, 1).eq(User::getId, userId).update();
        return Constants.UserActivation.SUCCESS;
    }

    @Override
    public Map<String, Object> userLogin(String username, String password, int expireSeconds) {
        HashMap<String, Object> result = new HashMap<>(4);

        User user = this.getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (ObjCheckUtils.isNull(user)) {
            result.put("resultCode", Constants.Login.USERNAME_ERROR);
            return result;
        }
        String encodePassword = EncodeUtils.encodePassword(password, user.getSalt());
        if (!user.getPassword().equals(encodePassword)) {
            result.put("resultCode", Constants.Login.PASSWORD_ERROR);
            return result;
        }

        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(user.getId());
        ticket.setExpired(LocalDateTime.now().plusSeconds(expireSeconds));
        ticket.setTicket(EncodeUtils.generateTicket(ticket.getUserId()));
        loginTicketService.save(ticket);

        result.put("ticket", ticket.getTicket());
        return result;
    }

    @Override
    public void userLogout(String ticket) {
        loginTicketService.lambdaUpdate()
                .set(LoginTicket::getStatus, 1)
                .eq(LoginTicket::getTicket, ticket)
                .update();
    }


    @Transactional
    @Override
    public void updatePassword(String newPassword, String ticket, User user) {
        newPassword = EncodeUtils.encodePassword(newPassword,user.getSalt());
        this.lambdaUpdate()
                .set(User::getPassword, newPassword)
                .eq(User::getId, user.getId())
                .update();
        this.userLogout(ticket);
    }
}
