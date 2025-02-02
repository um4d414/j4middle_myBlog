package ru.umd.myblog.app.service;

import javassist.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.umd.myblog.app.config.service.ServiceTestConfig;
import ru.umd.myblog.app.data.dto.CommentDto;
import ru.umd.myblog.app.data.entity.Comment;
import ru.umd.myblog.app.data.repository.CommentRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServiceTestConfig.class})
public class DefaultCommentServiceMockedRepoTest {
    @Autowired
    private DefaultCommentService defaultCommentService;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void shouldReturnCommentsByPostId() {
        // Подготавливаем тестовые данные
        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setContent("Test comment 1");
        comment1.setPostId(100L);

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setContent("Test comment 2");
        comment2.setPostId(100L);

        when(commentRepository.findByPostId(100L)).thenReturn(List.of(comment1, comment2));

        // Вызываем метод сервиса
        List<CommentDto> dtos = defaultCommentService.getCommentsByPostId(100L);
        assertEquals(2, dtos.size(), "Для поста с id=100 должно быть 2 комментария");

        CommentDto dto1 = dtos.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("Test comment 1", dto1.getContent());
        assertEquals(100L, dto1.getPostId());

        CommentDto dto2 = dtos.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals("Test comment 2", dto2.getContent());
        assertEquals(100L, dto2.getPostId());
    }

    @Test
    void shouldAddComment() {
        // При добавлении комментария вызывается save у репозитория
        long postId = 200L;
        String content = "New test comment";

        // При вызове save можно вернуть созданный комментарий с сгенерированным id
        Comment savedComment = new Comment();
        savedComment.setId(10L);
        savedComment.setContent(content);
        savedComment.setPostId(postId);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // Вызываем метод сервиса
        defaultCommentService.addComment(postId, content);

        // Проверяем, что метод save был вызван с объектом, содержащим корректные данные
        verify(commentRepository, times(1))
            .save(argThat(
                comment ->
                    comment.getContent().equals(content) &&
                    comment.getPostId().equals(postId)
            ));
    }

    @Test
    void shouldUpdateComment() throws NotFoundException {
        long commentId = 5L;
        String newContent = "Updated comment content";

        // Подготавливаем тестовый комментарий
        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setContent("Old content");
        existingComment.setPostId(300L);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        // Вызываем метод обновления
        defaultCommentService.updateComment(commentId, newContent);

        // Проверяем, что метод update был вызван с объектом, у которого обновлено содержимое
        verify(commentRepository, times(1)).update(argThat(comment ->
                                                               comment.getId() == commentId && comment.getContent()
                                                                   .equals(newContent)
        ));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistingComment() {
        long nonExistingId = 999L;
        when(commentRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> defaultCommentService.updateComment(nonExistingId, "Some content")
        );

        assertEquals("Комментарий с ID " + nonExistingId + " не найден", exception.getMessage());
    }

    @Test
    void shouldDeleteComment() {
        long commentId = 7L;
        // Вызываем метод удаления
        defaultCommentService.deleteComment(commentId);
        // Проверяем, что метод deleteById был вызван
        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    void shouldReturnCommentById() {
        long commentId = 3L;
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setContent("Test comment");
        comment.setPostId(400L);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        Optional<CommentDto> result = defaultCommentService.findCommentById(commentId);
        assertTrue(result.isPresent());
        CommentDto dto = result.get();
        assertEquals(commentId, dto.getId());
        assertEquals("Test comment", dto.getContent());
        assertEquals(400L, dto.getPostId());
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistingComment() {
        long nonExistingId = 888L;
        when(commentRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Optional<CommentDto> result = defaultCommentService.findCommentById(nonExistingId);
        assertFalse(result.isPresent());
    }
}
