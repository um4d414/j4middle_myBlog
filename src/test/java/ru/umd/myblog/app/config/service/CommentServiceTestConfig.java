package ru.umd.myblog.app.config.service;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.umd.myblog.app.data.repository.CommentRepository;
import ru.umd.myblog.app.service.DefaultCommentService;

@Configuration
public class CommentServiceTestConfig {

    @Bean
    public CommentRepository commentRepository() {
        return Mockito.mock(CommentRepository.class);
    }

    @Bean
    public DefaultCommentService commentService(CommentRepository commentRepository) {
        return new DefaultCommentService(commentRepository);
    }
}