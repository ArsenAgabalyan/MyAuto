package com.example.myauto.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        org.springframework.security.web.savedrequest.HttpSessionRequestCache requestCache = new org.springframework.security.web.savedrequest.HttpSessionRequestCache();
        requestCache.setRequestMatcher(request -> !request.getRequestURI().startsWith("/api/"));

        http
                .csrf(csrf -> csrf.disable())
                .requestCache(cache -> cache.requestCache(requestCache))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/", "/auth/login", "/auth/register", "/login", "/register", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/*.css", "/*.js").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/listings/add", "/listings/edit/**", "/listings/delete/**", "/listings/approve/**").authenticated()
                        .requestMatchers("/listings/{id}").permitAll()

                        // Доступ к профилю только авторизованным
                        .requestMatchers("/profile/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/", false)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}