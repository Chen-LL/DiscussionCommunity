package com.kuney.community.interceptor;

import com.kuney.community.application.entity.User;
import com.kuney.community.application.service.UserService;
import com.kuney.community.util.*;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author kuneychen
 * @since 2022/6/12 21:59
 */
@Component
@AllArgsConstructor
public class LoginTokenInterceptor implements HandlerInterceptor {

    private UserService userService;
    private HostHolder hostHolder;
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = CookieUtils.getValue(request, "token");
        if (ObjCheckUtils.nonBlank(token)) {
            int userId = EncodeUtils.parseUserId(token);
            Object storeToken = redisTemplate.opsForValue().get(RedisKeyUtils.getLoginTokenKey(userId));
            if (token.equals(storeToken)) {
                User user = userService.getUser(userId);
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            modelAndView.addObject("loginUser", hostHolder.getUser());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
