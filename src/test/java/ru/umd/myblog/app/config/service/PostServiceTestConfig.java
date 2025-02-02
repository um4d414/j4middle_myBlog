package ru.umd.myblog.app.config.service;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.umd.myblog.app.data.repository.PostRepository;
import ru.umd.myblog.app.service.DefaultPostService;
import ru.umd.myblog.app.service.ImageService;

@Configuration
public class PostServiceTestConfig {

    @Bean
    public PostRepository postRepository() {
        return Mockito.mock(PostRepository.class);
    }

    @Bean
    public ImageService imageService() {
        return Mockito.mock(ImageService.class);
    }

    @Bean
    public DefaultPostService postService(PostRepository postRepository) {
        return new DefaultPostService(postRepository);
    }
}
