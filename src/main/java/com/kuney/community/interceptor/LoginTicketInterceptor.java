package com.kuney.community.interceptor;

import com.kuney.community.application.entity.User;
import com.kuney.community.application.service.UserService;
import com.kuney.community.util.CookieUtils;
import com.kuney.community.util.HostHolder;
import com.kuney.community.util.ObjCheckUtils;
import com.kuney.community.util.RedisKeyUtils;
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
public class LoginTicketInterceptor implements HandlerInterceptor {

    // private LoginTicketService loginTicketService;
    private UserService userService;
    private HostHolder hostHolder;
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtils.getValue(request, "ticket");
        if (ticket != null) {
            // LoginTicket loginTicket = loginTicketService
            //         .getOne(Wrappers.<LoginTicket>lambdaQuery().eq(LoginTicket::getTicket, ticket));
            // 检查凭证是否有效
            // if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().isAfter(LocalDateTime.now())) {
            //     User user = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getId, loginTicket.getUserId()));
            //     hostHolder.setUser(user);
            // }
            Integer userId = (Integer) redisTemplate.opsForValue().get(RedisKeyUtils.getLoginTicketKey(ticket));
            if (ObjCheckUtils.nonNull(userId)) {
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
