package ru.umd.myblog.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import ru.umd.myblog.app.config.AppConfig;
import ru.umd.myblog.app.controller.CommentController;
import ru.umd.myblog.app.controller.PostController;
import ru.umd.myblog.app.data.repository.*;
import ru.umd.myblog.app.service.*;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { AppConfig.class })
@WebAppConfiguration
public class AppContextTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    void testDataSourceExists() {
        DataSource dataSource = context.getBean(DataSource.class);
        assertNotNull(dataSource, "DataSource должен быть создан");
    }

    @Test
    void testJdbcTemplateExists() {
        JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
        assertNotNull(jdbcTemplate, "JdbcTemplate должен быть создан");
    }

    @Test
    void testPostRepositoryBeansLoaded() {
        Object postRepository = context.getBean(PostRepository.class);
        assertNotNull(postRepository, "PostRepository должен быть создан");
    }

    @Test
    void testCommentRepositoryBeansLoaded() {
        Object commentRepository = context.getBean(CommentRepository.class);
        assertNotNull(commentRepository, "CommentRepository должен быть создан");
    }

    @Test
    void testCommentServiceBeansLoaded() {
        CommentService commentService = context.getBean(CommentService.class);
        assertNotNull(commentService, "CommentService должен быть создан");
    }

    @Test
    void testPostServiceBeansLoaded() {
        PostService postService = context.getBean(PostService.class);
        assertNotNull(postService, "PostService должен быть создан");
    }

    @Test
    void testImageServiceBeansLoaded() {
        ImageService imageService = context.getBean(ImageService.class);
        assertNotNull(imageService, "ImageService должен быть создан");
    }

    @Test
    void testPostControllerBeansLoaded() {
        PostController postController = context.getBean(PostController.class);
        assertNotNull(postController, "PostController должен быть создан");
    }


    @Test
    void testCommentControllerBeansLoaded() {
        CommentController commentController = context.getBean(CommentController.class);
        assertNotNull(commentController, "CommentController должен быть создан");
    }

    @Test
    void testMultipartResolverExists() {
        MultipartResolver multipartResolver = context.getBean(MultipartResolver.class);
        assertNotNull(multipartResolver, "MultipartResolver должен быть создан");
    }

    @Test
    void testThymeleafViewResolverExists() {
        ViewResolver viewResolver = context.getBean(ViewResolver.class);
        assertNotNull(viewResolver, "Thymeleaf ViewResolver должен быть создан");
    }

    @Test
    void testWebApplicationContext() {
        assertTrue(context instanceof WebApplicationContext, "Контекст должен быть экземпляром WebApplicationContext");
    }
}