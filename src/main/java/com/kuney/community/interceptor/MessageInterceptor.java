package com.kuney.community.interceptor;

import com.kuney.community.application.entity.User;
import com.kuney.community.application.service.MessageService;
import com.kuney.community.util.HostHolder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author kuneychen
 * @since 2022/6/25 19:39
 */
@Component
@AllArgsConstructor
public class MessageInterceptor implements HandlerInterceptor {

    private HostHolder hostHolder;
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            int unreadLetter = messageService.countUnreadLetter(user.getId());
            int unreadNotice = messageService.countUnreadNotice(user.getId());
            modelAndView.addObject("totalUnread", unreadLetter + unreadNotice);
        }
    }
}
