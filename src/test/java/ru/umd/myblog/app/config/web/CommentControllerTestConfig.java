package ru.umd.myblog.app.config.web;

import org.mockito.Mockito;
import org.springframework.context.annotation.*;
import ru.umd.myblog.app.controller.CommentController;
import ru.umd.myblog.app.service.CommentService;

@Configuration
@ComponentScan(basePackageClasses = CommentController.class)
public class CommentControllerTestConfig {

    @Bean
    public CommentService commentService() {
        return Mockito.mock(CommentService.class);
    }
}
