package com.diepau.todolist.config;

import com.diepau.todolist.security.JwtCookieFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Central Spring Security setup: public pages, JWT filter, stateless (no server session).
@Configuration
public class SecurityConfig {

    private final JwtCookieFilter jwtCookieFilter;

    public SecurityConfig(JwtCookieFilter jwtCookieFilter) {
        this.jwtCookieFilter = jwtCookieFilter;
    }

    // BCrypt: one-way password hashing.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Stop Spring Boot from auto-registering the JWT filter globally; it must run only inside the chain below.
    @Bean
    public FilterRegistrationBean<JwtCookieFilter> jwtFilterDisableAutoRegistration(JwtCookieFilter filter) {
        FilterRegistrationBean<JwtCookieFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // Allow the H2 console to render inside frames.
            .headers(h -> h.frameOptions(f -> f.disable()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(e -> e.authenticationEntryPoint(
                (req, res, ex) -> res.sendRedirect("/login")))
            .formLogin(f -> f.disable())
            .httpBasic(b -> b.disable())
            .addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
