package com.ctwi.config;


import com.ctwi.filter.AuthenticationFilter;
import com.ctwi.service.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    private final SessionManager service;
    @Autowired
    public FilterConfig(SessionManager service) {
        this.service = service;
    }

//    @Bean
//    public FilterRegistrationBean<AuthenticationFilter> authenticationFilter() {
//        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
//        registrationBean.setFilter(new AuthenticationFilter(service));
////        registrationBean.addUrlPatterns("/api/");
//        return registrationBean;
//    }
}
