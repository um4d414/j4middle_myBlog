package ru.umd.myblog.app.config.web;

import org.springframework.context.annotation.*;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@Import({
    CommentControllerTestConfig.class,
    PostControllerTestConfig.class,
    TemplateResolverTestConfig.class
})
public class ControllerTestConfig {
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
