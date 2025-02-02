package ru.umd.myblog.app.data.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.umd.myblog.app.config.data.RepositoryTestConfig;
import ru.umd.myblog.app.data.entity.Comment;
import ru.umd.myblog.app.data.repository.JdbcNativeCommentRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RepositoryTestConfig.class)
@Sql(
    scripts = {
        "/sql/cleanup-all.sql",
        "/sql/comment/data.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class JdbcNativeCommentRepositoryTest {
    @Autowired
    private JdbcNativeCommentRepository repository;

    @Test
    void shouldReturnAllCommentsForGivenPostId() {
        // Получаем список комментариев для поста с id=1
        List<Comment> comments = repository.findByPostId(1L);
        assertNotNull(comments);
        // Для поста с id=1 должно быть 2 комментария
        assertEquals(2, comments.size(), "Для поста с id=1 должно быть 2 комментария");

        Comment firstComment = comments.get(0);
        assertNotNull(firstComment.getContent());
        assertEquals(1L, firstComment.getPostId());
    }

    @Test
    void shouldSaveCommentAndAssignGeneratedId() {
        Comment comment = new Comment();
        // Устанавливаем содержимое комментария на английском
        comment.setContent("Test comment");
        comment.setPostId(1L);

        Comment savedComment = repository.save(comment);
        assertNotNull(savedComment.getId(), "Сохранённый комментарий должен иметь сгенерированный id");

        Optional<Comment> fetched = repository.findById(savedComment.getId());
        assertTrue(fetched.isPresent(), "Комментарий должен присутствовать в БД");
        // Проверяем, что содержимое комментария на английском
        assertEquals("Test comment", fetched.get().getContent());
        assertEquals(1L, fetched.get().getPostId());
    }

    @Test
    void shouldDeleteCommentById() {
        Comment comment = new Comment();
        // Устанавливаем содержимое комментария на английском
        comment.setContent("Comment for deletion");
        comment.setPostId(1L);
        Comment savedComment = repository.save(comment);
        Long commentId = savedComment.getId();

        repository.deleteById(commentId);

        Optional<Comment> fetched = repository.findById(commentId);
        assertFalse(fetched.isPresent(), "Комментарий должен быть удалён");
    }

    @Test
    void shouldUpdateCommentContentSuccessfully() {
        Comment comment = new Comment();
        // Устанавливаем исходное содержимое комментария на английском
        comment.setContent("Original content");
        comment.setPostId(1L);
        Comment savedComment = repository.save(comment);

        // Обновляем содержимое комментария на английском
        savedComment.setContent("Updated content");
        repository.update(savedComment);

        Optional<Comment> fetched = repository.findById(savedComment.getId());
        assertTrue(fetched.isPresent(), "Обновлённый комментарий должен присутствовать в БД");
        // Проверяем, что содержимое обновлено и на английском
        assertEquals("Updated content", fetched.get().getContent());
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistingComment() {
        Optional<Comment> existing = repository.findById(1L);
        assertTrue(existing.isPresent(), "Комментарий с id=1 должен существовать");

        Optional<Comment> nonExisting = repository.findById(999L);
        assertFalse(nonExisting.isPresent(), "Комментарий с id=999 не должен существовать");
    }
}
