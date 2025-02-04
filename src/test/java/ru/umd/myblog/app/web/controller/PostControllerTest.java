package ru.umd.myblog.app.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.umd.myblog.app.config.web.ControllerTestConfig;
import ru.umd.myblog.app.data.dto.*;
import ru.umd.myblog.app.service.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {ControllerTestConfig.class})
public class PostControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ImageService imageService;

    @BeforeEach
    void setup() {
        // Создаем MockMvc с использованием WebApplicationContext
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    /**
     * Тест для GET /posts с фильтрацией по тегу и пагинацией.
     */
    @Test
    void testPostsFeed() throws Exception {
        // Подготавливаем тестовый PostsPage
        PostDto post1 = PostDto.builder()
            .id(1L)
            .title("First Post")
            .content("Content of the first post")
            .imageUrl("/uploads/img1.jpg")
            .likes(10)
            .tags(Set.of("tech", "java"))
            .build();
        PostDto post2 = PostDto.builder()
            .id(2L)
            .title("Second Post")
            .content("Content of the second post")
            .imageUrl("/uploads/img2.jpg")
            .likes(5)
            .tags(Set.of("spring", "jdbc"))
            .build();

        PostsPage postsPage = PostsPage.builder()
            .content(List.of(post1, post2))
            .currentPage(0)
            .pageSize(10)
            .totalPosts(2)
            .totalPages(1)
            .build();

        when(postService.getPosts(eq("tech"), eq(0), eq(10))).thenReturn(postsPage);

        mockMvc.perform(get("/posts")
                            .param("tag", "tech")
                            .param("page", "0")
                            .param("size", "10")
                            .accept(MediaType.TEXT_HTML))
            .andExpect(result -> {
                // Проверяем, что статус 200 и view name равен "postfeed"
                assertEquals(200, result.getResponse().getStatus());
                String viewName = result.getModelAndView().getViewName();
                assertEquals("postfeed", viewName, "View name должен быть 'postfeed'");
                // Дополнительно можно проверить наличие атрибутов модели
                Object postsAttr = result.getModelAndView().getModel().get("posts");
                assertEquals(2, ((List<?>) postsAttr).size(), "Должно быть 2 поста");
            });
    }

    /**
     * Тест для создания поста (POST /posts) с multipart данными.
     */
    @Test
    void testCreatePost() throws Exception {
        // Подготавливаем входные данные
        String tagsString = "new, test";
        // Симулируем загрузку файла (не пустой файл)
        byte[] fileContent = "dummy image content".getBytes();
        MockMultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", fileContent);

        // При вызове imageService.saveImage возвращаем URL изображения
        when(imageService.saveImage(any())).thenReturn("/uploads/test.jpg");

        // Подготавливаем входной PostDto (без тегов и imageUrl)
        PostDto inputDto = PostDto.builder()
            .title("New Post")
            .content("New content")
            .build();
        // Результат создания поста с сгенерированным id
        PostDto createdDto = PostDto.builder()
            .id(3L)
            .title("New Post")
            .content("New content")
            .imageUrl("/uploads/test.jpg")
            .likes(0)
            .tags(Set.of("new", "test"))
            .build();
        when(postService.createPost(any(PostDto.class))).thenReturn(createdDto);

        mockMvc.perform(multipart("/posts")
                            .file(imageFile)
                            .param("title", "New Post")
                            .param("content", "New content")
                            .param("tagsString", tagsString))
            .andExpect(result -> {
                // Ожидается редирект на /posts
                String redirectedUrl = result.getResponse().getRedirectedUrl();
                assertEquals("/posts", redirectedUrl, "Должен быть редирект на /posts");
            });

        verify(imageService, times(1)).saveImage(any());
        verify(postService, times(1)).createPost(any(PostDto.class));
    }

    /**
     * Тест для лайка поста (POST /posts/{postId}/like).
     */
    @Test
    void testLikePost() throws Exception {
        when(postService.likePost(1L)).thenReturn(11);

        mockMvc.perform(post("/posts/1/like")
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(result -> {
                assertEquals(200, result.getResponse().getStatus());
                assertEquals(MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentType());
                String json = result.getResponse().getContentAsString();
                // Простой разбор JSON через ObjectMapper
                @SuppressWarnings("unchecked")
                var map = objectMapper.readValue(json, java.util.Map.class);
                assertEquals(11, map.get("likes"));
            });
    }

    /**
     * Тест для просмотра поста (GET /posts/{postId}).
     */
    @Test
    void testGetPost() throws Exception {
        PostDto postDto = PostDto.builder()
            .id(1L)
            .title("First Post")
            .content("Content of the first post")
            .imageUrl("/uploads/img1.jpg")
            .likes(10)
            .tags(Set.of("tech", "java"))
            .build();
        when(postService.findPostById(1L)).thenReturn(Optional.of(postDto));

        CommentDto comment1 = CommentDto.builder().id(1L).content("Nice post!").postId(1L).build();
        CommentDto comment2 = CommentDto.builder().id(2L).content("I agree!").postId(1L).build();
        when(commentService.getCommentsByPostId(1L)).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/posts/1").accept(MediaType.TEXT_HTML))
            .andExpect(result -> {
                assertEquals(200, result.getResponse().getStatus());
                String viewName = result.getModelAndView().getViewName();
                assertEquals("post", viewName, "View name должен быть 'post'");
                // Проверяем, что в модели есть атрибуты post и comments
                Object postAttr = result.getModelAndView().getModel().get("post");
                Object commentsAttr = result.getModelAndView().getModel().get("comments");
                assertEquals(postDto, postAttr);
                assertEquals(2, ((List<?>) commentsAttr).size());
            });
    }

    /**
     * Тест для редактирования поста (POST /posts/edit).
     */
    @Test
    void testEditPost() throws Exception {
        mockMvc.perform(post("/posts/edit")
                            .param("id", "1")
                            .param("title", "Updated Title")
                            .param("content", "Updated Content")
                            .param("imageUrl", "/uploads/updated.jpg")
                            .param("likes", "15")
                            .param("tags", "updated, java"))
            .andExpect(result -> {
                String redirectedUrl = result.getResponse().getRedirectedUrl();
                assertEquals("/posts/1", redirectedUrl, "Должен быть редирект на /posts/1");
            });

        verify(postService, times(1)).updatePost(argThat(new ArgumentMatcher<PostDto>() {

            @Override
            public boolean matches(PostDto dto) {
                return dto.getId() == 1L &&
                       "Updated Title".equals(dto.getTitle()) &&
                       "Updated Content".equals(dto.getContent()) &&
                       "/uploads/updated.jpg".equals(dto.getImageUrl()) &&
                       dto.getLikes() == 15 &&
                       dto.getTags() != null &&
                       dto.getTags().equals(Set.of("updated", "java"));
            }
        }));
    }

    /**
     * Тест для удаления поста (POST /posts/{postId}/delete).
     */
    @Test
    void testDeletePost() throws Exception {
        doNothing().when(postService).deletePost(1L);

        mockMvc.perform(post("/posts/1/delete")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(result -> {
                String redirectedUrl = result.getResponse().getRedirectedUrl();
                assertEquals("/posts", redirectedUrl, "Должен быть редирект на /posts");
            });

        verify(postService, times(1)).deletePost(1L);
    }
}
