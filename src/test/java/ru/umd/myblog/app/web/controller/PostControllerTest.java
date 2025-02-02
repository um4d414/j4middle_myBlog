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
import ru.umd.myblog.app.data.dto.PostDto;
import ru.umd.myblog.app.service.PostService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ContextConfiguration(classes = {ControllerTestConfig.class})
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
public class PostControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private PostService postService;

    @BeforeEach
    void setup() {
        // Настраиваем MockMvc с реальным веб-контекстом (с Thymeleaf)
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    // Тест для GET /posts, который должен вернуть страницу ленты постов (шаблон postfeed.html)
    @Test
    void testPostsFeedRendersCorrectTemplate() throws Exception {
        // Подготавливаем данные для сервиса
        when(postService.getPosts()).thenReturn(List.of(
            PostDto.builder()
                .id(1L)
                .title("First Post")
                .content("Content of the first post")
                .imageUrl("http://example.com/image1.jpg")
                .likes(10)
                .tags(Set.of("tech", "java"))
                .build(),
            PostDto.builder()
                .id(2L)
                .title("Second Post")
                .content("Content of the second post")
                .imageUrl("http://example.com/image2.jpg")
                .likes(5)
                .tags(Set.of("spring", "jdbc"))
                .build()
        ));

        mockMvc.perform(get("/posts").accept(MediaType.TEXT_HTML))
            .andExpect(result -> {
                // Проверяем статус ответа
                assertEquals(200, result.getResponse().getStatus());
                // Проверяем, что view name равно "postfeed"
                String viewName = result.getModelAndView().getViewName();
                assertEquals("postfeed", viewName, "View name должен быть 'postfeed'");
                // Проверяем содержимое HTML-ответа
                String content = result.getResponse().getContentAsString();
                assertTrue(content.contains("<title>Лента постов</title>"), "Должен быть заголовок 'Лента постов'");
                assertTrue(content.contains("Добавить пост"), "Должна присутствовать ссылка на создание поста");
            });
    }

    // Тест для создания поста (POST /posts) с пустым файлом (image)
    @Test
    void testCreatePostWithoutImageFile() throws Exception {
        PostDto savedDto = PostDto.builder()
            .id(3L)
            .title("New Post")
            .content("New content")
            .tags(Set.of("new", "test"))
            .build();
        when(postService.createPost(any(PostDto.class))).thenReturn(savedDto);

        mockMvc.perform(multipart("/posts")
                            .file(new MockMultipartFile("image", new byte[0]))
                            .param("title", "New Post")
                            .param("content", "New content")
                            .param("tags", "new,test")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(result -> {
                // Проверяем, что происходит редирект на /posts
                String redirectedUrl = result.getResponse().getRedirectedUrl();
                assertEquals("/posts", redirectedUrl, "Должен быть редирект на /posts");
            });

        verify(postService, times(1)).createPost(any(PostDto.class));
    }

    // Тест для лайка поста (POST /posts/{postId}/like) – возвращается JSON с новым количеством лайков
    @Test
    void testLikePostReturnsJson() throws Exception {
        when(postService.likePost(1L)).thenReturn(11);

        mockMvc.perform(post("/posts/1/like")
                            .accept(MediaType.APPLICATION_JSON)) // Явно указываем, что ожидаем JSON
            .andExpect(result -> {
                // Проверяем статус ответа и content type
                assertEquals(200, result.getResponse().getStatus());
                assertEquals(MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentType());
                // Используем ObjectMapper для разбора JSON
                String json = result.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                Map<String, Object> map = objectMapper.readValue(json, Map.class);
                assertEquals(11, map.get("likes"));
            });
    }

    // Тест для GET /posts/{postId}, который возвращает страницу поста (шаблон post.html) с комментариями
    @Test
    void testGetPostRendersCorrectTemplate() throws Exception {
        PostDto postDto = PostDto.builder()
            .id(1L)
            .title("First Post")
            .content("Content of the first post")
            .imageUrl("http://example.com/image1.jpg")
            .likes(10)
            .tags(Set.of("tech", "java"))
            .build();
        when(postService.findPostById(1L)).thenReturn(Optional.of(postDto));

        mockMvc.perform(get("/posts/1"))
            .andExpect(result -> {
                // Проверяем статус ответа
                assertEquals(200, result.getResponse().getStatus());
                // Проверяем, что view name равно "post"
                String viewName = result.getModelAndView().getViewName();
                assertEquals("post", viewName, "View name должен быть 'post'");
                // Проверяем, что HTML содержит заголовок поста и контент
                String content = result.getResponse().getContentAsString();
                assertTrue(
                    content.contains("<title>First Post</title>"),
                    "Заголовок страницы должен содержать 'First Post'"
                );
                assertTrue(content.contains("Content of the first post"), "Должен быть виден контент поста");
            });
    }

    // Тест для редактирования поста (POST /posts/edit) – проверяем редирект и корректность передачи параметров
    @Test
    void testEditPostRedirects() throws Exception {
        mockMvc.perform(post("/posts/edit")
                            .param("id", "1")
                            .param("title", "Updated Title")
                            .param("content", "Updated Content")
                            .param("imageUrl", "http://example.com/updated.jpg")
                            .param("likes", "15")
                            .param("tags", "updated,java"))
            .andExpect(result -> {
                // Проверяем редирект на /posts/1
                String redirectedUrl = result.getResponse().getRedirectedUrl();
                assertEquals("/posts/1", redirectedUrl, "Должен быть редирект на /posts/1");
            });

        verify(postService, times(1)).updatePost(argThat(new ArgumentMatcher<PostDto>() {

            @Override
            public boolean matches(PostDto dto) {
                return dto.getId() == 1L &&
                       "Updated Title".equals(dto.getTitle()) &&
                       "Updated Content".equals(dto.getContent()) &&
                       "http://example.com/updated.jpg".equals(dto.getImageUrl()) &&
                       dto.getLikes() == 15 &&
                       dto.getTags().equals(Set.of("updated", "java"));
            }
        }));
    }

    // Тест для удаления поста (POST /posts/{postId}/delete)
    @Test
    void testDeletePostRedirects() throws Exception {
        mockMvc.perform(post("/posts/1/delete"))
            .andExpect(result -> {
                String redirectedUrl = result.getResponse().getRedirectedUrl();
                assertEquals("/posts", redirectedUrl, "Должен быть редирект на /posts");
            });

        verify(postService, times(1)).deletePost(1L);
    }
}