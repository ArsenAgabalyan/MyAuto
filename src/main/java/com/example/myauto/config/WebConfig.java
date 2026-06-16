package com.example.myauto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Определяем физический путь к папке uploads в корне вашего проекта
        String uploadDir = System.getProperty("user.dir") + "/uploads/";

        // Связываем виртуальный URL-путь /uploads/** с реальной папкой на диске
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }
}