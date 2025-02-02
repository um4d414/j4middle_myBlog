package ru.umd.myblog.app.data.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.umd.myblog.app.config.data.RepositoryTestConfig;
import ru.umd.myblog.app.data.entity.Post;
import ru.umd.myblog.app.data.repository.JdbcNativePostRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RepositoryTestConfig.class)
@Sql(
    scripts = {
        "/sql/cleanup-all.sql",
        "/sql/post/data.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class JdbcNativePostRepositoryTest {

    @Autowired
    private JdbcNativePostRepository repository;

    @Test
    void shouldReturnPostById() {
        // Получаем пост по id=1
        Optional<Post> postOptional = repository.getPost(1L);
        assertTrue(postOptional.isPresent(), "Пост с id=1 должен существовать");

        Post post = postOptional.get();
        // Проверяем поля поста
        assertEquals("First Post", post.getTitle());
        assertEquals("http://example.com/image1.jpg", post.getImageUrl());
        assertEquals("Content of the first post", post.getContent());
        assertEquals(10, post.getLikes());
        assertEquals(Set.of("tech", "java"), post.getTags());
    }

    @Test
    void shouldReturnAllPosts() {
        // Получаем все посты
        List<Post> posts = repository.getAllPosts();
        // Ожидаем, что их будет 2
        assertEquals(2, posts.size(), "Должно быть 2 поста");

        Post firstPost = posts.get(0);
        // Проверяем первый пост
        assertEquals("First Post", firstPost.getTitle());
        assertEquals("http://example.com/image1.jpg", firstPost.getImageUrl());
        assertEquals("Content of the first post", firstPost.getContent());
        assertEquals(10, firstPost.getLikes());
        assertEquals(Set.of("tech", "java"), firstPost.getTags());

        Post secondPost = posts.get(1);
        // Проверяем второй пост
        assertEquals("Second Post", secondPost.getTitle());
        assertEquals("http://example.com/image2.jpg", secondPost.getImageUrl());
        assertEquals("Content of the second post", secondPost.getContent());
        assertEquals(5, secondPost.getLikes());
        assertEquals(Set.of("spring", "jdbc"), secondPost.getTags());
    }

    @Test
    void shouldCreateNewPost() {
        // Создаем новый пост с данными на английском
        Post newPost = new Post();
        newPost.setTitle("New Post");
        newPost.setImageUrl("http://example.com/new.jpg");
        newPost.setContent("New content");
        newPost.setTags(Set.of("new", "test"));

        Post createdPost = repository.createPost(newPost);
        // Проверяем, что у созданного поста сгенерирован id и корректно установлены поля
        assertNotNull(createdPost.getId(), "Новый пост должен иметь сгенерированный id");
        assertEquals("New Post", createdPost.getTitle());
        assertEquals("http://example.com/new.jpg", createdPost.getImageUrl());
        assertEquals("New content", createdPost.getContent());
        assertEquals(0, createdPost.getLikes());
        assertEquals(Set.of("new", "test"), createdPost.getTags());
    }

    @Test
    void shouldIncrementPostLikes() {
        // Увеличиваем количество лайков для поста с id=1
        int likes = repository.incrementLikes(1L);
        assertEquals(11, likes, "Количество лайков должно увеличиться до 11");

        Optional<Post> postOptional = repository.getPost(1L);
        assertTrue(postOptional.isPresent(), "Пост с id=1 должен существовать");
        assertEquals(11, postOptional.get().getLikes(), "Количество лайков у поста должно быть 11");
    }

    @Test
    void shouldUpdatePostDetails() {
        // Получаем пост с id=1 и обновляем его данные
        Post post = repository.getPost(1L).orElseThrow();
        post.setTitle("Updated Title");
        post.setContent("Updated Content");
        post.setImageUrl("http://example.com/updated.jpg");
        post.setLikes(15);
        post.setTags(Set.of("updated", "java"));

        repository.updatePost(post);

        Post updatedPost = repository.getPost(1L).orElseThrow();
        // Проверяем, что данные поста обновились
        assertEquals("Updated Title", updatedPost.getTitle());
        assertEquals("Updated Content", updatedPost.getContent());
        assertEquals("http://example.com/updated.jpg", updatedPost.getImageUrl());
        assertEquals(15, updatedPost.getLikes());
        assertEquals(Set.of("updated", "java"), updatedPost.getTags());
    }

    @Test
    void shouldDeletePostById() {
        // Удаляем пост с id=1
        repository.deletePost(1L);

        Optional<Post> postOptional = repository.getPost(1L);
        assertFalse(postOptional.isPresent(), "Пост с id=1 должен быть удалён");
    }
}
