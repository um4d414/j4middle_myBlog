package ru.umd.myblog.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.umd.myblog.app.data.dto.CommentDto;
import ru.umd.myblog.app.data.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public void addComment(Long postId, String content) {
        // Логика добавления комментария
    }

    public void updateComment(Long commentId, String content) {
        // Логика обновления комментария
    }

    public void deleteComment(Long commentId) {
        // Логика удаления комментария
    }

    public CommentDto getCommentById(Long commentId) {
        // Логика получения комментария по ID
        return null;
    }

    public Long getPostIdByCommentId(Long commentId) {
        // Логика получения ID поста по ID комментария
        return null;
    }
}
