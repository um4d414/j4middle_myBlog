package ru.umd.myblog.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.umd.myblog.app.data.dto.CommentDto;
import ru.umd.myblog.app.data.dto.PostDto;
import ru.umd.myblog.app.data.entity.Comment;
import ru.umd.myblog.app.data.entity.Post;
import ru.umd.myblog.app.data.repository.CommentRepository;

import java.util.List;

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

    public List<CommentDto> getCommentsByPostId(Long postId) {
        return commentRepository
            .findByPostId(postId)
            .stream()
            .map(this::mapComment)
            .toList();
    }

    private CommentDto mapComment(Comment comment) {
        return CommentDto
            .builder()
            .id(comment.getId())
            .content(comment.getContent())
            .postId(comment.getPostId())
            .build();
    }

    private Comment mapComment(CommentDto commentDto) {
        var comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setContent(commentDto.getContent());
        comment.setPostId(commentDto.getPostId());

        return comment;
    }
}
