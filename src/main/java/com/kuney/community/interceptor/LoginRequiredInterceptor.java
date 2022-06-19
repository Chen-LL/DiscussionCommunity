package com.kuney.community.interceptor;

import com.kuney.community.annotation.LoginRequired;
import com.kuney.community.exception.CustomException;
import com.kuney.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author kuneychen
 * @since 2022/6/12 21:48
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);
            if (loginRequired != null && hostHolder.getUser() == null) {
                String requestType = request.getHeader("x-requested-with");
                if ("XMLHttpRequest".equals(requestType)) {
                    throw new CustomException(403, "请先登录");
                } else {
                    response.sendRedirect(request.getContextPath() + "/user/login");
                }
                return false;
            }
        }
        return true;
    }
}
