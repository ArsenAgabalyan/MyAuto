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
        http
                // Отключаем CSRF временно для упрощения работы с формами без токенов.
                // Примечание: Для продакшена защиту CSRF необходимо включить!
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                        // Разрешенные всем страницы
                        .requestMatchers("/", "/auth/login", "/auth/register", "/error").permitAll()

                        // Статика: CSS, JS, изображения, загруженные файлы
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/*.css", "/*.js").permitAll()

                        // ИСПРАВЛЕНО: Доступ к URL-адресам админки строго для пользователей с ROLE_ADMIN
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                        // Все остальные действия требуют авторизации
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/", true)
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
        // ВНИМАНИЕ: NoOpPasswordEncoder используется только для учебных целей.
        // В продакшене замените на: return new BCryptPasswordEncoder();
        return NoOpPasswordEncoder.getInstance();
    }
}