package ru.umd.myblog.app.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import ru.umd.myblog.app.config.AppConfig;

@ExtendWith(SpringExtension.class)
@SpringJUnitWebConfig(classes = {AppConfig.class})
@ActiveProfiles("test")
class PostServiceTest {
    @Autowired
    private PostService postService;

    @Test
    void getPosts() {
        var posts = postService.getPosts();
        Assertions.assertNotNull(posts);
    }

    @Test
    void findPost() {
    }

    @Test
    void createPost() {
    }
}