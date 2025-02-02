package ru.umd.myblog.app.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@Import({
    CommentControllerTestConfig.class,
    PostControllerTestConfig.class,
    TemplateResolverTestConfig.class
})
@EnableWebMvc
public class ControllerTestConfig {

}
