package com.change.qrcode.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(2)
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class UserSecurityConfig extends WebSecurityConfigurerAdapter {
    /*
    @Autowired
    private UserService userService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder2() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider2() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userService);
        auth.setPasswordEncoder(passwordEncoder2());
        return auth;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider2());
    }


     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.antMatcher("/user/**")
                .authorizeRequests().anyRequest().hasRole("USER")
                .and()
                .formLogin()
                .loginPage("/user/login")
                .loginProcessingUrl("/user/login")
                .defaultSuccessUrl("/user/home")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/user/login");
    }
}
