package com.kuney.community.annotation;

import com.kuney.community.util.Constants.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kuneychen
 * @since 2022/7/5 19:10
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {

    int role() default Role.USER;

}
