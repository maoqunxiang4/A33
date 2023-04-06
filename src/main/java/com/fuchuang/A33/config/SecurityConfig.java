package com.fuchuang.A33.config;

import com.fuchuang.A33.filter.TokenAuthenticationFilter;
import com.fuchuang.A33.service.Impl.Security.AccessDeniedHandlerImpl;
import com.fuchuang.A33.service.Impl.Security.AuthenticationEntryPointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private TokenAuthenticationFilter tokenAuthenticationFilter ;

    @Autowired
    private AuthenticationEntryPointImpl authenticationEntryPoint ;

    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler ;

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder() ;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests().antMatchers("/employee/login","/employee/regist").anonymous()
                .antMatchers("/v2/**","/swagger*/**").permitAll()
                .antMatchers(("/test/**")).permitAll()
                .anyRequest().authenticated() ;

        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) ;

        http.cors() ;

        http.exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint) ;
    }
}
