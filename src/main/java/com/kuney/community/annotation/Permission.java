package com.kuney.community.annotation;

import com.kuney.community.util.Constants.Role;

/**
 * @author kuneychen
 * @since 2022/7/5 19:10
 */
public @interface Permission {

    int role() default Role.USER;

}
