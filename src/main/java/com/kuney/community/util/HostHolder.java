package com.kuney.community.util;

import com.kuney.community.application.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author kuneychen
 * @since 2022/6/12 21:59
 */
@Component
public class HostHolder {

    private ThreadLocal<User> threadLocal = new ThreadLocal<>();

    public void setUser(User user) {
        threadLocal.set(user);
    }

    public User getUser() {
        return threadLocal.get();
    }

    public void clear() {
        threadLocal.remove();
    }
}
