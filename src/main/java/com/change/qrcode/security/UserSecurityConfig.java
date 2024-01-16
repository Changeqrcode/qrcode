package com.change.qrcode.security;

import com.change.qrcode.exception.MyAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@Order(2)
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class UserSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests().antMatchers("/user/login/qr/**",
                "/user/logout/qr/**","/user/resultPackages").permitAll();

        http.antMatcher("/user/**")
                .authorizeRequests().anyRequest().hasRole("USER")
                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler);
    }
}
