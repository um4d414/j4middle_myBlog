package ru.umd.myblog.app.config.web;

import org.mockito.Mockito;
import org.springframework.context.annotation.*;
import ru.umd.myblog.app.controller.PostController;
import ru.umd.myblog.app.service.ImageService;
import ru.umd.myblog.app.service.PostService;

@Configuration
@ComponentScan(basePackageClasses = PostController.class)
public class PostControllerTestConfig {
    @Bean
    public ImageService imageService() {
        return Mockito.mock(ImageService.class);
    }

    @Bean
    public PostService postService() {
        return Mockito.mock(PostService.class);
    }
}
