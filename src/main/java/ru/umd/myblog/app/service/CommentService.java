package ru.umd.myblog.app.service;

import javassist.NotFoundException;
import ru.umd.myblog.app.data.dto.CommentDto;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    List<CommentDto> getCommentsByPostId(long postId);

    void addComment(long postId, String content);

    void updateComment(long commentId, String content) throws NotFoundException;

    void deleteComment(long commentId);

    Optional<CommentDto> findCommentById(long commentId);
}
