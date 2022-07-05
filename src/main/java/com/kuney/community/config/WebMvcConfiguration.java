package com.kuney.community.config;

import com.kuney.community.interceptor.AuthenticateInterceptor;
import com.kuney.community.interceptor.DataInterceptor;
import com.kuney.community.interceptor.LoginTicketInterceptor;
import com.kuney.community.interceptor.MessageInterceptor;
import com.kuney.community.util.converter.LocalDateConverter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * @author kuneychen
 * @since 2022/6/12 21:51
 */
@Configuration
@AllArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

    private AuthenticateInterceptor authenticateInterceptor;
    private LoginTicketInterceptor loginTicketInterceptor;
    private MessageInterceptor messageInterceptor;
    private DataInterceptor dataInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> patterns = Arrays.asList("/error", "/**/*.css", "/**/*.js", "/**/*.png ", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(loginTicketInterceptor).excludePathPatterns(patterns);
        registry.addInterceptor(authenticateInterceptor).excludePathPatterns(patterns);
        registry.addInterceptor(messageInterceptor).excludePathPatterns(patterns);
        registry.addInterceptor(dataInterceptor).excludePathPatterns(patterns);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/error").setViewName("error/500");
        registry.addRedirectViewController("/", "/index");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new LocalDateConverter());
    }
}
