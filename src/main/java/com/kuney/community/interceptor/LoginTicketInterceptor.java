package com.kuney.community.interceptor;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kuney.community.application.entity.LoginTicket;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.service.LoginTicketService;
import com.kuney.community.application.service.UserService;
import com.kuney.community.util.CookieUtils;
import com.kuney.community.util.HostHolder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

/**
 * @author kuneychen
 * @since 2022/6/12 21:59
 */
@Component
@AllArgsConstructor
public class LoginTicketInterceptor implements HandlerInterceptor {

    private LoginTicketService loginTicketService;
    private UserService userService;
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtils.getValue(request, "ticket");
        if (ticket != null) {
            LoginTicket loginTicket = loginTicketService
                    .getOne(Wrappers.<LoginTicket>lambdaQuery().eq(LoginTicket::getTicket, ticket));
            // 检查凭证是否有效
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().isAfter(LocalDateTime.now())) {
                User user = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getId, loginTicket.getUserId()));
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
