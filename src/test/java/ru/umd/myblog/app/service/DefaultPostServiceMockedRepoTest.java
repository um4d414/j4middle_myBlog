package ru.umd.myblog.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.umd.myblog.app.config.service.ServiceTestConfig;
import ru.umd.myblog.app.data.dto.PostDto;
import ru.umd.myblog.app.data.entity.Post;
import ru.umd.myblog.app.data.repository.PostRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServiceTestConfig.class})
public class DefaultPostServiceMockedRepoTest {
    @Autowired
    private DefaultPostService defaultPostService;

    @Autowired
    private PostRepository postRepository;

    @Test
    void shouldReturnAllPosts() {
        // Подготавливаем тестовые данные: список постов
        Post post1 = new Post();
        post1.setId(1L);
        post1.setTitle("First Post");
        post1.setContent("Content of the first post");
        post1.setImageUrl("http://example.com/image1.jpg");
        post1.setLikes(10);
        post1.setTags(Set.of("tech", "java"));

        Post post2 = new Post();
        post2.setId(2L);
        post2.setTitle("Second Post");
        post2.setContent("Content of the second post");
        post2.setImageUrl("http://example.com/image2.jpg");
        post2.setLikes(5);
        post2.setTags(Set.of("spring", "jdbc"));

        when(postRepository.getAllPosts()).thenReturn(List.of(post1, post2));

        // Вызываем метод сервиса
        List<PostDto> posts = defaultPostService.getPosts();

        // Проверяем, что маппинг выполнен корректно
        assertEquals(2, posts.size());
        PostDto dto1 = posts.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("First Post", dto1.getTitle());
        assertEquals("Content of the first post", dto1.getContent());
        assertEquals("http://example.com/image1.jpg", dto1.getImageUrl());
        assertEquals(10, dto1.getLikes());
        assertEquals(Set.of("tech", "java"), dto1.getTags());

        PostDto dto2 = posts.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals("Second Post", dto2.getTitle());
        assertEquals("Content of the second post", dto2.getContent());
        assertEquals("http://example.com/image2.jpg", dto2.getImageUrl());
        assertEquals(5, dto2.getLikes());
        assertEquals(Set.of("spring", "jdbc"), dto2.getTags());
    }

    @Test
    void shouldReturnPostByIdWhenExists() {
        // Подготавливаем тестовый пост
        Post post = new Post();
        post.setId(1L);
        post.setTitle("First Post");
        post.setContent("Content of the first post");
        post.setImageUrl("http://example.com/image1.jpg");
        post.setLikes(10);
        post.setTags(Set.of("tech", "java"));

        when(postRepository.getPost(1L)).thenReturn(Optional.of(post));

        Optional<PostDto> result = defaultPostService.findPostById(1L);
        assertTrue(result.isPresent());
        PostDto dto = result.get();
        assertEquals(1L, dto.getId());
        assertEquals("First Post", dto.getTitle());
        assertEquals("Content of the first post", dto.getContent());
        assertEquals("http://example.com/image1.jpg", dto.getImageUrl());
        assertEquals(10, dto.getLikes());
        assertEquals(Set.of("tech", "java"), dto.getTags());
    }

    @Test
    void shouldReturnEmptyOptionalWhenPostNotFound() {
        when(postRepository.getPost(999L)).thenReturn(Optional.empty());
        Optional<PostDto> result = defaultPostService.findPostById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    void shouldCreateNewPost() {
        // Подготавливаем входные данные: PostDto без id
        PostDto inputDto = PostDto.builder()
            .title("New Post")
            .content("New content")
            .imageUrl("http://example.com/new.jpg")
            .likes(0)
            .tags(Set.of("new", "test"))
            .build();

        // Создаем объект Post для маппинга (без id)
        Post inputPost = new Post();
        inputPost.setTitle("New Post");
        inputPost.setContent("New content");
        inputPost.setImageUrl("http://example.com/new.jpg");
        inputPost.setLikes(0);
        inputPost.setTags(Set.of("new", "test"));

        // Результат сохранения: сгенерированный id
        Post savedPost = new Post();
        savedPost.setId(3L);
        savedPost.setTitle("New Post");
        savedPost.setContent("New content");
        savedPost.setImageUrl("http://example.com/new.jpg");
        savedPost.setLikes(0);
        savedPost.setTags(Set.of("new", "test"));

        when(postRepository.createPost(any(Post.class))).thenReturn(savedPost);

        PostDto resultDto = defaultPostService.createPost(inputDto);
        assertNotNull(resultDto.getId());
        assertEquals(3L, resultDto.getId());
        assertEquals("New Post", resultDto.getTitle());
        assertEquals("New content", resultDto.getContent());
        assertEquals("http://example.com/new.jpg", resultDto.getImageUrl());
        assertEquals(0, resultDto.getLikes());
        assertEquals(Set.of("new", "test"), resultDto.getTags());
    }

    @Test
    void shouldIncrementPostLikes() {
        when(postRepository.incrementLikes(1L)).thenReturn(11);
        int updatedLikes = defaultPostService.likePost(1L);
        assertEquals(11, updatedLikes);
    }

    @Test
    void shouldUpdatePostDetails() {
        // Подготавливаем входной PostDto с обновленными данными
        PostDto updateDto = PostDto.builder()
            .id(1L)
            .title("Updated Title")
            .content("Updated Content")
            .imageUrl("http://example.com/updated.jpg")
            .likes(15)
            .tags(Set.of("updated", "java"))
            .build();

        // Вызываем метод обновления
        defaultPostService.updatePost(updateDto);

        // Проверяем, что репозиторий получил объект Post с корректными значениями
        verify(postRepository, times(1))
            .updatePost(argThat(
                post ->
                    post.getId() == (1L) &&
                    post.getTitle().equals("Updated Title") &&
                    post.getContent().equals("Updated Content") &&
                    post.getImageUrl().equals("http://example.com/updated.jpg") &&
                    post.getLikes() == 15 &&
                    post.getTags().equals(Set.of("updated", "java"))
            ));
    }

    @Test
    void shouldDeletePostById() {
        defaultPostService.deletePost(1L);
        verify(postRepository, times(1)).deletePost(1L);
    }
}
