package ru.umd.myblog.app.data.repository;

import ru.umd.myblog.app.data.dto.CommentDto;

public interface CommentRepository {
    void addComment(Long postId, String content);

    void updateComment(Long commentId, String content);

    void deleteComment(Long commentId); //

    CommentDto getCommentById(Long commentId);

    Long getPostIdByCommentId(Long commentId);
}
