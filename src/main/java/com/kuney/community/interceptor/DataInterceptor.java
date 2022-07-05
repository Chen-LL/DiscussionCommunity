package com.kuney.community.interceptor;

import com.kuney.community.application.entity.User;
import com.kuney.community.application.service.DataService;
import com.kuney.community.util.HostHolder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author kuneychen
 * @since 2022/7/5 21:41
 */
@Component
@AllArgsConstructor
public class DataInterceptor implements HandlerInterceptor {

    private DataService dataService;
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        dataService.recordUV(request.getRemoteHost());
        User user = hostHolder.getUser();
        if (user != null) {
            dataService.recordDAU(user.getId());
        }
        return true;
    }
}
