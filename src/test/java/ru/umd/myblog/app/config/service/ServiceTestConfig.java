package ru.umd.myblog.app.config.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
    PostServiceTestConfig.class,
    CommentServiceTestConfig.class,
    ImageServiceTestConfig.class,
})
@Configuration()
public class ServiceTestConfig {
}
