package com.moe.socialnetwork.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/google-login",
                                "/api/auth//change-password", "/api/auth//password-reset-request",
                                "/api/auth/password-reset", "/api/auth/refresh-token", "/api/auth//logout",
                                "api/file/upload-image", "api/file/upload-video", "/api/file/upload-audio",
                                "/api/file/upload-any")
                        .permitAll()
                        .requestMatchers("/api/upload/image").hasAuthority("ADMIN_INSERT") // demo
                        .anyRequest().authenticated())
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
