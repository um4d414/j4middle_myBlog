package ru.umd.myblog.app.config.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.umd.myblog.app.service.DefaultImageService;

@Configuration
public class ImageServiceTestConfig {

    @Bean
    public DefaultImageService imageService() {
        return new DefaultImageService();
    }
}
