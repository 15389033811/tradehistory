package com.example.demo.configuration;

import com.example.demo.interceptor.RequestCountInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfigration implements WebMvcConfigurer {

    @Bean
    public RequestCountInterceptor getRequestCountInterceptor(){ return new RequestCountInterceptor();}


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(getRequestCountInterceptor());

    }

}
