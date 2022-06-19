package com.kuney.community.config;

import com.kuney.community.interceptor.LoginRequiredInterceptor;
import com.kuney.community.interceptor.LoginTicketInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
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

    private LoginRequiredInterceptor loginRequiredInterceptor;
    private LoginTicketInterceptor loginTicketInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> patterns = Arrays.asList("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns(patterns);
        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns(patterns);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/error").setViewName("error/500");
    }
}
